/*
 * Copyright (C) 2019 - 2020 Rabobank Nederland
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.rabobank.argos.service.domain.release;

import com.rabobank.argos.domain.account.AccountKeyInfo;
import com.rabobank.argos.domain.crypto.PublicKey;
import com.rabobank.argos.domain.hierarchy.HierarchyMode;
import com.rabobank.argos.domain.layout.LayoutMetaBlock;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.release.ReleaseDossier;
import com.rabobank.argos.domain.release.ReleaseDossierMetaData;
import com.rabobank.argos.domain.release.ReleaseResult;
import com.rabobank.argos.service.domain.NotFoundException;
import com.rabobank.argos.service.domain.account.AccountInfoRepository;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import com.rabobank.argos.service.domain.verification.VerificationProvider;
import com.rabobank.argos.service.domain.verification.VerificationRunResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.rabobank.argos.domain.SupplyChainHelper.reversePath;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReleaseServiceImpl implements ReleaseService {

    private final VerificationProvider verificationProvider;
    private final LayoutMetaBlockRepository layoutMetaBlockRepository;
    private final ReleaseRepository releaseRepository;
    private final AccountInfoRepository accountInfoRepository;
    private final HierarchyRepository hierarchyRepository;
    private final LinkMetaBlockRepository linkMetaBlockRepository;

    @Override
    public ReleaseResult createRelease(String supplyChainId, List<Set<Artifact>> releaseArtifacts) {
        log.info("Release Artifacts [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);

        String supplyChainPath = getSupplyChainPath(supplyChainId);
        List<List<String>> releaseArtifactHashes = convertToReleaseArtifactHashes(releaseArtifacts);
        return releaseRepository
                .findReleaseByReleasedArtifactsAndPath(releaseArtifactHashes, supplyChainPath)
                .map(releaseDossierMetaData -> {
                    log.info("Artifacts already released [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
                    return ReleaseResult
                        .builder()
                        .releaseIsValid(true)
                        .releaseDossierMetaData(releaseDossierMetaData)
                        .build();
                }
                )
                .orElseGet(() -> verifyAndStoreRelease(supplyChainId, releaseArtifacts, supplyChainPath, releaseArtifactHashes));
    }

    private ReleaseResult verifyAndStoreRelease(String supplyChainId, List<Set<Artifact>> releaseArtifacts, String supplyChainPath, List<List<String>> releaseArtifactHashes) {
        ReleaseResult.ReleaseResultBuilder releaseBuilder = ReleaseResult.builder();
        Optional<LayoutMetaBlock> optionalLayoutMetaBlock = layoutMetaBlockRepository.findBySupplyChainId(supplyChainId);
        if (optionalLayoutMetaBlock.isPresent()) {

            List<Artifact> allArtifacts = releaseArtifacts
                    .stream()
                    .flatMap(Collection::stream)
                    .collect(Collectors.toList());

            VerificationRunResult verificationRunResult = verificationProvider.verifyRun(optionalLayoutMetaBlock.get(), allArtifacts);
            releaseBuilder.releaseIsValid(verificationRunResult.isRunIsValid());

            if (verificationRunResult.isRunIsValid()) {
                ReleaseDossierMetaData releaseDossierMetaData = createAndStoreRelease(
                        supplyChainPath,
                        optionalLayoutMetaBlock.get(),
                        verificationRunResult,
                        releaseArtifactHashes);
                releaseBuilder.releaseDossierMetaData(releaseDossierMetaData);
                linkMetaBlockRepository.deleteBySupplyChainId(supplyChainId);
            }
            log.info("Artifacts released [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
            return releaseBuilder.build();
        }
        log.info("Artifacts release invalid [{}] for supply chain [{}].", releaseArtifacts, supplyChainId);
        return ReleaseResult.builder().releaseIsValid(false).build();
    }

    private ReleaseDossierMetaData createAndStoreRelease(String supplyChainPath, LayoutMetaBlock layoutMetaBlock,
                                                         VerificationRunResult verificationRunResult,
                                                         List<List<String>> releaseArtifacts) {

        List<ReleaseDossier.Account> accounts = getAccounts(layoutMetaBlock);

        ReleaseDossierMetaData releaseDossierMetaData = ReleaseDossierMetaData.builder()
                .releaseArtifacts(releaseArtifacts)
                .supplyChainPath(supplyChainPath)
                .build();

        ReleaseDossier releaseDossier = ReleaseDossier.builder()
                .layoutMetaBlock(layoutMetaBlock)
                .linkMetaBlocks(verificationRunResult.getValidLinkMetaBlocks())
                .accounts(accounts)
                .build();
        releaseRepository.storeRelease(releaseDossierMetaData, releaseDossier);
        return releaseDossierMetaData;
    }

    private List<List<String>> convertToReleaseArtifactHashes(List<Set<Artifact>> releaseArtifacts) {
        return releaseArtifacts
                .stream()
                .map(s -> s.stream()
                        .map(Artifact::getHash)
                        .collect(Collectors.toList()))
                .collect(Collectors.toList());
    }

    private String getSupplyChainPath(String supplyChainId) {
        return hierarchyRepository.getSubTree(supplyChainId, HierarchyMode.NONE, 0)
                .map(treeNode -> String.join(".", reversePath(treeNode.getPathToRoot())) + "." + treeNode.getName())
                .orElseThrow(() -> new NotFoundException("Supplychain not found"));
    }

    private List<ReleaseDossier.Account> getAccounts(LayoutMetaBlock layoutMetaBlock) {
        List<String> keyIds = layoutMetaBlock
                .getLayout()
                .getKeys()
                .stream()
                .map(PublicKey::getKeyId)
                .collect(Collectors.toList());

        return accountInfoRepository.findByKeyIds(keyIds).stream()
                .map(toAccount()
                ).collect(Collectors.toList());
    }

    private static Function<AccountKeyInfo, ReleaseDossier.Account> toAccount() {
        return acountInfo ->
                ReleaseDossier
                        .Account
                        .builder()
                        .id(acountInfo.getAccountId())
                        .keyId(acountInfo.getKey().getKeyId())
                        .name(acountInfo.getName())
                        .path(String.join(".", reversePath(acountInfo.getPathToRoot())))
                        .type(acountInfo.getAccountType()).build();
    }
}
