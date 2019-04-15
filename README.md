# Local Deployment

## Deploy insights-inventory

rhsm-conduit requires a connection to insights-inventory. First set up a
postgres user and database.

```
su - postgres
createuser --pwprompt -d insights
```

Run the `bin/deploy-insights` script to install the insights-inventory
project and begin running it on port 8080 by default. Check the `--help`
to see all the available options. This script will init the
git-submodule if it hasn't been already, run the database migration from
`manage.py` and then start the Flask application with the appropriate
environment variables.

Once the app has started, check to make sure you can access the API
(keep in mind that you may need to adjust the port number in the curl
command if you used a different port for deployment).

```
curl http://localhost:8080/metrics
```

## Build and Run rhsm-conduit

In order to build rhsm-conduit, make sure you have Java SDK 8 installed
(Java 1.8.x).

Build and run using the following line:

```
./gradlew assemble && java -jar build/libs/rhsm-conduit-1.0.0.jar
```

# Remote Deployment

## OpenShift Project Set Up

Choose a project to deploy to, and set up a `rhsm-conduit-config`
ConfigMap for that project. There is an example config files in
`openshift/example_config/rhsm-conduit.conf`, that can be applied via:

```
oc create configmap rhsm-conduit-config --from-file openshift/example_config
```

Also, set up rhsm-conduit secrets:

```
oc create -f openshift/secret-rhsm-conduit_dummy.yaml
```

The secrets are used for client certificates, so having an empty secret
is acceptable if client certificates are not used.

## Deploy insights-inventory

First you'll need to deploy an instance of postgres.
```
oc new-app --template=postgresql-persistent -p POSTGRESQL_USER=insights -p POSTGRESQL_PASSWORD=insights -p POSTGRESQL_DATABASE=inventory
```

NOTE: The hostname of the database service will be
`postgres.<project_namespace>.svc`. You can use the console to find the
hostname via: Applications -> Services -> postgresql

Create a new insights-inventory template. Take note of the route for the
new app.

```
oc create -f openshift/template_insights-inventory.yaml
oc new-app --template=rhsm-insights-inventory -p INVENTORY_DB_HOSTNAME=<YOUR_DATABASE_HOSTNAME> -p INVENTORY_SHARED_SECRET=<YOUR_SHARED_SECRET>
```

Test the installation:
```
curl http://<your_route_address>/metrics
```

## Deploy to Openshift

First, log in to an openshift instance. Make sure the project has been
set up (see previous section).

```
# add a template for deploying rhsm-conduit
oc create -f openshift/template_rhsm-conduit.yaml
oc new-app --template=rhsm-conduit  # deploy an instance of rhsm-conduit using the template
```

By default, the template deploys the master branch of rhsm-conduit. If
it's more appropriate to deploy a different branch (e.g. production),
then use:

```
oc new-app --template=rhsm-conduit -p SOURCE_REPOSITORY_REF=production
```

If, for debugging on a local machine, for convenience, you need a route
to test rhsm-conduit,

```
oc create -f openshift/template_rhsm-conduit-route.yaml
oc new-app --template=rhsm-conduit-route
```

## Kafka

See the detailed notes [here](README-kafka.md)
