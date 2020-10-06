#!/bin/bash

set -x
set -m

/entrypoint.sh couchbase-server &

while [ "$(curl -Isw '%{http_code}' -o /dev/null http://localhost:8091/ui/index.html#/)" != 200 ]; do
    sleep 5
done

# Setup cluster
couchbase-cli cluster-init \
    --cluster 127.0.0.1:8091 \
    --services data,query,index \
    --cluster-ramsize 256 \
    --cluster-index-ramsize 256 \
    --cluster-username Administrator \
    --cluster-password password

# Setup bucket
couchbase-cli bucket-create \
    --cluster 127.0.0.1:8091 \
    --username Administrator \
    --password password \
    --bucket example \
    --bucket-type couchbase \
    --bucket-ramsize 256

# Setup user
couchbase-cli user-manage \
    --cluster 127.0.0.1:8091 \
    --username Administrator \
    --password password \
    --set \
    --rbac-username example \
    --rbac-password password \
    --roles bucket_full_access[example] \
    --auth-domain local

echo "Type: $TYPE"

if [ "$TYPE" = "WORKER" ]; then
    echo "Sleeping ..."
    sleep 15

    #IP=`hostname -s`
    IP=$(hostname -I | cut -d ' ' -f1)
    echo "IP: " $IP

    echo "Auto Rebalance: $AUTO_REBALANCE"
    if [ "$AUTO_REBALANCE" = "true" ]; then
        couchbase-cli rebalance --cluster=$COUCHBASE_MASTER:8091 --user=Administrator --password=password --server-add=$IP --server-add-username=Administrator --server-add-password=password
    else
        couchbase-cli server-add --cluster=$COUCHBASE_MASTER:8091 --user=Administrator --password=password --server-add=$IP --server-add-username=Administrator --server-add-password=password
    fi
fi

fg 1
