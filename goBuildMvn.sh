#!/bin/bash -x

# needed in docker-compose.yml -> don't remove, otherwise docker is executed as sudo and is writing files with root ownership!
#  export UID=$(id -u $whoami)
#  export GID=$(id -g $whoami)

#  echo "DEBUG: running build process with chown $(id -u):$(id -g)"

#run all tests and generate reports
if [ "$EXECUTE_TESTS" = true ]; then
  export GID=$(id -g)
  export USER_ID=$(id -u)    #UID is a readonly variable
  export USER_NAME=$(whoami) #UID is a readonly variable
  export DOCKER_GROUP=$(cut -d: -f3 < <(getent group docker))
  echo 'Tests are going to be executed! Set EXECUTE_TESTS to false, if no test execution is required.'
  docker-compose up --exit-code-from avatar-test --force-recreate
  if [ $? -ne 0 ]; then
    echo "Test execution failed"
    exit 1
  else
    echo "Successfully finished all tests."
  fi
  container_id=$(docker-compose ps -q avatar-test)
  docker cp ${container_id}:/home/mvn-user/build/ target/
  docker-compose down # make sure containers are stopped
fi
