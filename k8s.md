# Introduction
The purpose of this document is to provide guidance and background on hosting the application in Kubernetes (k8s).

# Mount Types
## AWS EFS Mounts from On-Premise
See [this article](https://docs.aws.amazon.com/efs/latest/ug/efs-onpremises.html) on mounting an AWS Elastic File System (EFS) on an on-premise Linux machine.  This approach likely makes sense for files that are not trasactional.

Basic steps:
- ensure AWS EFS utils is installed.  See [this](https://docs.aws.amazon.com/efs/latest/ug/installing-amazon-efs-utils.html#installing-other-distro)
- ensure .aws/credentials file exists for user, root, and set to use IAM credentials appropriate for EFS access
- make sure region is set in the EFS config file, /etc/amazon/efs/efs-utils.conf, for 'dns_name_format' and 'region'
- sudo mkdir 'efs' under '/mnt' and 'chgrp tomcat efs'
- sudo chmod 640 /mnt/efs
- ensure /etc/hosts has an entry for the EFS target IPs.  See [this](https://docs.aws.amazon.com/efs/latest/ug/efs-onpremises.html#wt5-step2-get-efs-utils)
- edit /etc/fstab.  See [this](https://docs.aws.amazon.com/efs/latest/ug/mount-fs-auto-mount-onreboot.html)
- copy folders needed by c1ux OE containers (assumes that /mnt/efs is mounted to the AWS EFS access point):
```bash
sudo cp -R /apps/c1ux/Properties /mnt/efs/cp-properties
sudo cp -R /apps/c1ux/translation /mnt/efs/cp-translation
sudo cp -R /apps/c1ux/runtimes /mnt/efs/cp-runtimes
```
- Use 'keytool' to add the RRD self-signed ROOT, IS and POL certificates to the Amazon Corretto JVM cacerts keystore and copy it to the EFS volume folder, 'jvm-security'.  For example:
```sh
sudo keytool -importcert -file ~/downloads/ROOTCA-2.cer -cacerts -alias "RRD-ROOT-CA"
sudo keytool -importcert -file ~/downloads/POLCA2-2.cer -cacerts -alias "RRD-POL-CA"
sudo keytool -importcert -file ~/downloads/ISCA2-2.cer -cacerts -alias "RRD-IS-CA"
sudo cp cacerts /mnt/efs/jvm-security/cacerts
```

## AWS EFS Integration in EKS
For AWS EKS to use the AWS EFS Container Storage Interface (CSI) driver, the driver must be set up in the EKS cluster.  See  [this](https://docs.aws.amazon.com/eks/latest/userguide/efs-csi.html) and [this](https://www.youtube.com/watch?v=CV5jXn8Dqsw).

See [this](https://aws.amazon.com/premiumsupport/knowledge-center/eks-persistent-storage/) for walkthrough on how to setup EFS for an EKS cluster.
See [this](https://www.youtube.com/watch?v=CV5jXn8Dqsw) for video on how EFS can integrate with EKS.

## AWS DataSync
See [this page](https://docs.aws.amazon.com/datasync/latest/userguide/what-is-datasync.html) for information on AWS DataSync, which is likely the best approach to syncronizing transactional files such as order, item, and catalog related files, between the corresponding on-premise NFS mounts and the EFS access points used by the EKS cluster.

# Deploying to AWS Elastic Kubernetes Service (EKS)
The Azure Devops (ADO) build that creates and pushes the container image to the private AWS Elastic Container Registry (ECR) can be found [here](https://dev.azure.com/rrdonnelley/shared/_build?definitionId=2487&_a=summary).

[![Build status](https://dev.azure.com/rrdonnelley/shared/_apis/build/status/CustomPoint/C1UX-API%20container%20build)](https://dev.azure.com/rrdonnelley/shared/_build/latest?definitionId=2487)

The release definition for deploying the container to the RRD proof of concept AWS EKS cluster can be found [here](https://dev.azure.com/rrdonnelley/shared/_release?definitionId=23&view=mine&_a=releases).

[![Release status](https://vsrm.dev.azure.com/rrdonnelley/_apis/public/Release/badge/ddc24c71-8cff-41e1-afb0-72dc30ec2feb/23/57)](https://dev.azure.com/rrdonnelley/shared/_release?definitionId=23&view=mine&_a=releases)

## Troubleshooting Deployments
To see the detail on the pod:
```sh
kubectl describe pod -n c1ux
```

To see the logs for the pod:
```sh
kc logs -n c1ux [pod name]
```