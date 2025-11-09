# Helm Chart for Einoice Application

Creating Your Own Helm Chart

```shell
helm create pantrio
```

Create a secret for Nexus credentials

```shell
oc create secret docker-registry regcred --docker-server=container.registry.local --docker-username=docker --docker-password=docker
```

Helm dependencies update:

```shell
helm dependency update helm/pantrio
```

---

Verify that the chart is formatted properly:

```shell
helm lint helm/pantrio
```

Check the generated Kubernetes resources

```shell
helm template helm/pantrio .
```

Package helm chart

```shell
helm package helm/pantrio --version 0.1.0 --app-version 2.0.0
```

Push helm chart to Nexus repository

```shell
curl -u <username>:<password> https://registry.local/repository/helm-internal/ --upload-file pantrio-0.1.0.tgz -v
```

Add helm repository

```shell
helm repo add crosig-helm https://registry.local/repository/helm-internal/
```

Search for the chart in the repository

```shell
helm search repo crosig-helm
```

Update helm repositories

```shell
helm repo update
```

Install helm chart from repository

```shell
helm install pantrio crosig-helm/pantrio
```

Install from local chart

```shell
helm install pantrio ./pantrio-0.1.0.tgz

helm install pantrio ./pantrio-0.1.0.tgz -f ./helm/pantrio/application-dev.yaml
```

Verify that the chart has installed successfully:

```shell
helm list
```

Test the chart:

```shell
helm test pantrio --logs
```

Expose a service:

```shell
oc expose svc pantrio
```

Uninstall the chart:

```shell
helm uninstall pantrio
```

## Pushing Docker Image to Nexus Registry

Login to Nexus Docker registry:

```powershell
$env:DOCKER_PASSWORD="your_password_here"

$env:DOCKER_PASSWORD | docker login -u mmatokovic --password-stdin registry.local
```

Tag and build the image:

```shell
docker build -t registry.local/pantrio:2.0.0 .
```

Push to Nexus Docker registry:

```shell
docker push registry.local/pantrio:2.0.0
```
