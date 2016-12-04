#!/bin/bash -x

SBT_CONTAINER_VERSION="latest"



function init() {

  DEPENDENCY_LABEL=$GO_DEPENDENCY_LABEL_SBT_CONTAINER


  if [ -z ${DEPENDENCY_LABEL} ]; then
    SBT_CONTAINER_VERSION="latest"
  else
    SBT_CONTAINER_VERSION="v${DEPENDENCY_LABEL}"
  fi


}

function build_software() {
	
	# get local .ivy2
	rsync -r ~/.ivy2 ./.ivy2
  	docker run --user `id -u`:`id -g` --env AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID --env AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY  --volume=${PWD}:/build ubirch/sbt-build:${SBT_CONTAINER_VERSION} clean compile test
	# write back to local .ivy2
  	
  if [ $? -ne 0 ]; then
	  rsync -r ./.ivy2 ~/.ivy2 
      echo "Docker build failed"
      exit 1
  else
	  rsync -rv ./.ivy2 ~/.ivy2 
  fi
}

case "$1" in
    build)
        init
        build_software
        ;;
    *)
        echo "Usage: $0 {build|publish}"
        exit 1
esac

exit 0