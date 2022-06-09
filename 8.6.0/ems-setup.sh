docker pull eu.gcr.io/de-fo-gr-pr-shared/tibco/ems/ems-server:8.6.0

cd "C:\Users\ALCH390\GolandProjects\TIBCO-EMS\8.6.0"
docker-compose --file docker-compose-build-complete.yml build

kubectl create ns ems
cd "C:\Users\ALCH390\GolandProjects\TIBCO-EMS\8.6.0\helm\ems-service"
helm install -n ems ems-service -f minikube-values.yaml .

minikube tunnel