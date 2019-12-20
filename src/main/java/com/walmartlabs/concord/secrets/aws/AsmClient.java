package com.walmartlabs.concord.secrets.aws;

import com.amazonaws.auth.AWSCredentialsProviderChain;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.secretsmanager.AWSSecretsManager;
import com.amazonaws.services.secretsmanager.AWSSecretsManagerClientBuilder;
import com.amazonaws.services.secretsmanager.model.CreateSecretRequest;
import com.amazonaws.services.secretsmanager.model.CreateSecretResult;
import com.amazonaws.services.secretsmanager.model.DeleteSecretRequest;
import com.amazonaws.services.secretsmanager.model.DeleteSecretResult;
import com.amazonaws.services.secretsmanager.model.DescribeSecretRequest;
import com.amazonaws.services.secretsmanager.model.DescribeSecretResult;
import com.amazonaws.services.secretsmanager.model.GetSecretValueRequest;
import com.amazonaws.services.secretsmanager.model.GetSecretValueResult;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.google.common.collect.ImmutableList;
import com.walmartlabs.concord.secrets.SecretsProvider;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;


//
// Retrieve secrets from the AWS Secret Manager
//
// We need to account for the following:
//
// account/region/user|profile
//
//
public class AsmClient implements SecretsProvider {

    private final AWSSecretsManager client;

    public AsmClient(String region) {
        String endpoint = String.format("secretsmanager.%s.amazonaws.com", region);
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
        client = AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(new AWSCredentialsProviderChain(ImmutableList.of(
                        new ProfileCredentialsProvider(), // check the local ~/.aws/credentials
                        new ConcordCredentialsProvider()))) // check Concord's secrets store
                .build();
    }

    public AsmClient(String region, String awsAccessKey, String awsSecretKey) {
        String endpoint = String.format("secretsmanager.%s.amazonaws.com", region);
        AwsClientBuilder.EndpointConfiguration endpointConfiguration = new AwsClientBuilder.EndpointConfiguration(endpoint, region);
        client = AWSSecretsManagerClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration)
                .withCredentials(new AWSCredentialsProviderChain(ImmutableList.of(
                        new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKey, awsSecretKey)),
                        new ProfileCredentialsProvider(), // check the local ~/.aws/credentials
                        new ConcordCredentialsProvider()))) // check Concord's secrets store
                .build();
    }

    public List<String> secretsList() {

        // By default this will only return 20 secrets names. Would be nice if they let you provide
        // a filter to grab "kubeconfig-*" for example.
        ListSecretsRequest request = new ListSecretsRequest().withMaxResults(100);

        return client.listSecrets(request).getSecretList().stream()
                .map(SecretListEntry::getName)
                .collect(Collectors.toList());
    }


    public DescribeSecretResult describe(String secretName) {
        DescribeSecretRequest request = new DescribeSecretRequest().withSecretId(secretName);
        return client.describeSecret(request);
    }

    public void put(String secretName, String secretText) {
        CreateSecretRequest request = new CreateSecretRequest().withName(secretName).withSecretString(secretText);
        CreateSecretResult result = client.createSecret(request);

    }

    public void remove(String secretName) {
        DeleteSecretRequest request = new DeleteSecretRequest().withSecretId(secretName);
        DeleteSecretResult result = client.deleteSecret(request);
    }

    public String get(String secretName) {

        GetSecretValueRequest request = new GetSecretValueRequest().withSecretId(secretName);
        GetSecretValueResult result = client.getSecretValue(request);
        return result.getSecretString();
    }
}