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
import com.amazonaws.services.secretsmanager.model.InvalidParameterException;
import com.amazonaws.services.secretsmanager.model.InvalidRequestException;
import com.amazonaws.services.secretsmanager.model.ListSecretsRequest;
import com.amazonaws.services.secretsmanager.model.ResourceNotFoundException;
import com.amazonaws.services.secretsmanager.model.SecretListEntry;
import com.google.common.collect.ImmutableList;
import com.walmartlabs.concord.secrets.SecretsProvider;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.stream.Collectors;


//
// Retrieve secrets from the AWS Secrets Manager
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

        ListSecretsRequest request = new ListSecretsRequest();

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

    public void get(String secretName) {

        String secret;
        ByteBuffer binarySecretData;
        GetSecretValueRequest getSecretValueRequest = new GetSecretValueRequest().withSecretId(secretName);

        GetSecretValueResult getSecretValueResult = null;
        try {
            getSecretValueResult = client.getSecretValue(getSecretValueRequest);
        } catch (ResourceNotFoundException e) {
            System.out.println("The requested secret " + secretName + " was not found");
        } catch (InvalidRequestException e) {
            System.out.println("The request was invalid due to: " + e.getMessage());
        } catch (InvalidParameterException e) {
            System.out.println("The request had invalid params: " + e.getMessage());
        }

        if (getSecretValueResult == null) {
            return;
        }

        // Depending on whether the secret was a string or binary, one of these fields will be populated
        if (getSecretValueResult.getSecretString() != null) {
            secret = getSecretValueResult.getSecretString();
            System.out.println(secret);
        } else {
            binarySecretData = getSecretValueResult.getSecretBinary();
            System.out.println(binarySecretData);
        }
    }

    public static void main(String[] args) {
        String secretName = "kubeconfig-jvz-021-us-east-2-dev";
        AsmClient client = new AsmClient("us-west-2");
        List<String> secrets = client.secretsList();
        System.out.println("secrets = " + secrets);
        System.out.println(secrets.contains(secretName));
    }
}