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
	rsync -r ~/.ivy2 ./
  	docker run --user `id -u`:`id -g` --env AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID --env AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY  --volume=${PWD}:/build ubirch/sbt-build:${SBT_CONTAINER_VERSION} $1
	# write back to local .ivy2

  if [ $? -ne 0 ]; then
	  rsync -r ./.ivy2 ~/
      echo "Docker build failed"
      exit 1
  else
	  rsync -rv ./.ivy2 ~/
  fi
}

function build_container() {
  # copy artefacts to TMP directory for faster build
  mkdir -p TMP
  # get artefact name from Dockerfile
filename=`awk '/^ADD.*server-assembly.*/{ print$2}' Dockerfile`
  tar cvf - $filename | (cd TMP; tar xvf - )
  tar cvf - config/src/main/resources/ | (cd TMP; tar xvf - )
  cp Dockerfile TMP/
  cd TMP
  docker build -t ubirch-avatar-service .
  if [ $? -ne 0 ]; then
    echo "Docker build failed"
    exit 1
  fi

}
case "$1" in
    build)
        init
        build_software "clean compile test"
        ;;
    assembly)
        build_software "clean server/assembly"
        ;;
    containerbuild)
        build_container
        ;;
    *)
        echo "Usage: $0 {build|assembly|containerbuild}"
        exit 1
esac

exit 0
