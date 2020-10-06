FROM couchbase:community-6.6.0

COPY configure-node.sh /opt/couchbase

RUN ["chmod", "+x", "/opt/couchbase/configure-node.sh"]

CMD ["/opt/couchbase/configure-node.sh"]
