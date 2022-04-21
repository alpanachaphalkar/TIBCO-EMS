### Install okteto CLI using Scoop in Windows Powershell

Set-ExecutionPolicy RemoteSigned -scope CurrentUser
iwr -useb get.scoop.sh | iex
scoop install okteto

## copy jdk-11.0.4_linux-x64_bin.tar.gz
cp <path/to/jdk-11.0.4_linux-x64_bin.tar.gz> tibco/base/1.0/jdk-11.0.4_linux-x64_bin.tar.gz

### okteto build
okteto build

### okteto deploy
okteto deploy

### port-forwarding
kubectl port-forward -n ems-alpanachaphalkar service/ems-svc 30722:30722