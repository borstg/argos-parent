#!/bin/sh
#
# Copyright (C) 2019 - 2020 Rabobank Nederland
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#         http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

set -x
WIREMOCK_VERSION="2.25.1"
IMAGE_TAG=${WIREMOCK_VERSION}
REGISTRY="argosnotary"
IMAGE_NAME="oauth-stub"
build_image=${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}


image() {
  echo "Build image ${build_image}"
  docker build \
    --build-arg WIREMOCK_VERSION=${WIREMOCK_VERSION} \
    --tag ${build_image} \
  .
}

push() {
  image
  docker push ${build_image}
}

help() {
  echo "Usage: ./build.sh <function>"
  echo ""
  echo "Functions"
  printf "   \033[36m%-30s\033[0m %s\n" "image" "Build the Docker image."
  printf "   \033[36m%-30s\033[0m %s\n" "push_final" "Push the Docker image to the internal registry."
  echo ""
  echo "Content of build.config"
  printf "   \033[36m%-30s\033[0m %s\n" "name=<name>" "Name of the Docker image, required."
  echo ""
}

#sed -e "s/@@VERSION@@/$VERSION/g" docker/Dockerfile.template > docker/Dockerfile 

if [ -z "${1}" ]; then
  echo "ERROR: function required"
  help
  exit 1
fi
${1}