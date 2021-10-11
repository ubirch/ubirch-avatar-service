Import Production Log
=====================

## Requirements

1. access to source data files (managed by Beate Fiss)

    1. https://docs.google.com/spreadsheets/d/1pC9GJtIw3DlXqSy1rZoEXK989Uik3Rkov7wglR1jX3g/edit
    1. entry points *importProdLogs.prodLogs*
    
1. in case of used locally you need this services
 
    1. ElasticSearch
    2. mongoDB
    3. Redis
    4. Kafka
    5. mqtt
    6. UserService
    7. IdentityService
    8. AvatarService

1. use this script: *com.ubirch.avatar.cmd.ImportProdLogs* 

    1. parameter: -Dconfig.resource=application-import-local.conf
    
    1. env vars (trackle-dev):
    
        1. ES_HOST=970da16b74ddc9259ef1440d77f907f9.eu-west-1.aws.found.io
        1. ES_CLUSTER_NAME=970da16b74ddc9259ef1440d77f907f9
        1. ES_PORT=9343
        1. ELASTIC_IO_PASSWORD=***********************
        1. ELASTIC_IO_USER=elastic

## configure behavior

use this property file inside cmdtools modul: *application.admin.conf*

## idea behind the script

This script creates all missing trackle devices - just trackle devices !!! 

If *deleteExistingDevices*  is set to true existing devices will be deleted. 

Devices will be created without a owner, which enables later claiming. 
