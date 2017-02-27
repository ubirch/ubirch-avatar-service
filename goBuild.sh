#!/bin/bash -x

SBT_CONTAINER_VERSION="latest"



function init() {

  DEPENDENCY_LABEL=$GO_DEPENDENCY_LABEL_SBT_CONTAINER


  if [ -z ${DEPENDENCY_LABEL} ]; then
    SBT_CONTAINER_VERSION="latest"
  else
    SBT_CONTAINER_VERSION="v${DEPENDENCY_LABEL}"
  fi

  if [ -f Dockerfile.input  ]; then
    # clean up the artifact generated by the sbt build
    rm Dockerfile.input
  fi

}

function build_software() {

	# get local .ivy2
	rsync -r ~/.ivy2/ ./.ivy2/
  	docker run --user `id -u`:`id -g` --env AWS_ACCESS_KEY_ID=$AWS_ACCESS_KEY_ID --env AWS_SECRET_ACCESS_KEY=$AWS_SECRET_ACCESS_KEY  --volume=${PWD}:/build ubirch/sbt-build:${SBT_CONTAINER_VERSION} $1
	# write back to local .ivy2

  if [ $? -ne 0 ]; then
	  rsync -r ./.ivy2/ ~/.ivy2/
      echo "Docker build failed"
      exit 1
  else
	  rsync -r ./.ivy2/ ~/.ivy2/
  fi
}

function build_container() {
  # copy artefacts to TMP directory for faster build
  rm -rf TMP/
  mkdir -p TMP
  #get artifact names generated by Scala Build
  source Dockerfile.input
  if [ ! -f $SOURCE ]; then
    echo "Missing $SOURCE file \n did you run $0 assembly?"
    exit 1
  fi

  # get artefact name from Dockerfile

  tar cvf - $SOURCE | (cd TMP; tar xvf - )
  tar cvf - config/src/main/resources/ tools/ | (cd TMP; tar xvf - )
  cp Dockerfile.template TMP/Dockerfile
  #replace artefact name in start.sh
  sed -i.bak "s%@@build-artefact@@%$TARGET%g" TMP/tools/start.sh
  sed -i.bak "s%@@SOURCE@@%$SOURCE%g" TMP/Dockerfile
  sed -i.bak "s%@@TARGET@@%$TARGET%g" TMP/Dockerfile
  cd TMP
  docker build -t ubirch/ubirch-avatar-service:v$GO_PIPELINE_LABEL .

  if [ $? -ne 0 ]; then
    echo "Docker build failed"
    exit 1
  fi

  # push Docker image
  docker push ubirch/ubirch-avatar-service
  docker push ubirch/ubirch-avatar-service:v$GO_PIPELINE_LABEL
  if [ $? -ne 0 ]; then
    echo "Docker push failed"
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
        build_container
        ;;
    containerbuild)
        build_container
        ;;
    *)
        echo "Usage: $0 {build|assembly|containerbuild}"
        exit 1
esac

exit 0
