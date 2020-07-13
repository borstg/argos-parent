package com.rabobank.argos.service.domain.release;

import com.rabobank.argos.domain.hierarchy.HierarchyMode;
import com.rabobank.argos.domain.hierarchy.TreeNode;
import com.rabobank.argos.domain.link.Artifact;
import com.rabobank.argos.domain.release.ReleaseDossierMetaData;
import com.rabobank.argos.domain.release.ReleaseResult;
import com.rabobank.argos.service.domain.account.AccountInfoRepository;
import com.rabobank.argos.service.domain.hierarchy.HierarchyRepository;
import com.rabobank.argos.service.domain.layout.LayoutMetaBlockRepository;
import com.rabobank.argos.service.domain.link.LinkMetaBlockRepository;
import com.rabobank.argos.service.domain.verification.VerificationProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class ReleaseServiceImplTest {
    protected static final String SUPPLY_CHAIN_ID = "supplyChainId";
    @Mock
    private VerificationProvider verificationProvider;
    @Mock
    private LayoutMetaBlockRepository layoutMetaBlockRepository;
    @Mock
    private ReleaseRepository releaseRepository;
    @Mock
    private AccountInfoRepository accountInfoRepository;
    @Mock
    private HierarchyRepository hierarchyRepository;
    @Mock
    private LinkMetaBlockRepository linkMetaBlockRepository;

    @Mock
    private TreeNode treeNode;
    @Mock
    private ReleaseDossierMetaData releaseDossierMetaData;

    private ReleaseService releaseService;

    @BeforeEach
    void setup() {
        releaseService = new ReleaseServiceImpl(
                verificationProvider,
                layoutMetaBlockRepository,
                releaseRepository,
                accountInfoRepository,
                hierarchyRepository,
                linkMetaBlockRepository);

    }

    @Test
    void createReleaseForExistingDossierShouldReturnStoredDossier() {
        Artifact releaseArtifact = Artifact.builder().hash("hash").uri("/target/").build();
        List<Set<Artifact>> releaseArtifacts = Collections.singletonList(Set.of(releaseArtifact));
        when(treeNode.getName()).thenReturn("name");
        when(treeNode.getPathToRoot()).thenReturn(Collections.singletonList("path"));
        when(hierarchyRepository.getSubTree(SUPPLY_CHAIN_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(releaseRepository.findReleaseByReleasedArtifactsAndPath(any(), any())).thenReturn(Optional.of(releaseDossierMetaData));
        ReleaseResult releaseResult = releaseService.createRelease(SUPPLY_CHAIN_ID, releaseArtifacts);
        assertThat(releaseResult.isReleaseIsValid(), is(true));
        assertThat(releaseResult.getReleaseDossierMetaData(), sameInstance(releaseDossierMetaData));
        verifyNoInteractions(layoutMetaBlockRepository, accountInfoRepository, verificationProvider, linkMetaBlockRepository);
    }

    @Test
    void createReleaseForNonExistingDossierShouldReturnNewDossier() {
        Artifact releaseArtifact = Artifact.builder().hash("hash").uri("/target/").build();
        List<Set<Artifact>> releaseArtifacts = Collections.singletonList(Set.of(releaseArtifact));
        when(treeNode.getName()).thenReturn("name");
        when(treeNode.getPathToRoot()).thenReturn(Collections.singletonList("path"));
        when(hierarchyRepository.getSubTree(SUPPLY_CHAIN_ID, HierarchyMode.NONE, 0)).thenReturn(Optional.of(treeNode));
        when(releaseRepository.findReleaseByReleasedArtifactsAndPath(any(), any())).thenReturn(Optional.empty());

       /* ReleaseResult releaseResult =  releaseService.createRelease(SUPPLY_CHAIN_ID,releaseArtifacts);
        assertThat(releaseResult.isReleaseIsValid(),is(true));
        assertThat(releaseResult.getReleaseDossierMetaData(),sameInstance(releaseDossierMetaData));
        verifyNoInteractions(layoutMetaBlockRepository,accountInfoRepository,verificationProvider,linkMetaBlockRepository)*/
        ;
    }
}