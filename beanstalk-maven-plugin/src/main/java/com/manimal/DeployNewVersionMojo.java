package com.manimal;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import com.amazonaws.services.elasticbeanstalk.*;
import com.amazonaws.services.elasticbeanstalk.model.*;
import com.amazonaws.services.s3.*;
import com.amazonaws.auth.*;

import java.util.UUID;
import java.io.File;

/**
 * Goal which deploys a new Elastic Beanstalk version.
 * <p/>
 * There are three steps to deploying a new Elastic Beanstalk version.
 * 1. Upload the new war to S3.
 * 2. Create a new version for the application.
 * 3. Configure the specifc environments.
 *
 * @goal deployNewVersion
 * @phase beanstalk
 */
public class DeployNewVersionMojo extends AbstractMojo {

    private String awsAccessKey;
    private String aswSecretKey;

    private String s3Bucket;
    private String s3Key;
    private String war;

    private String ebAppName;
    private String ebNewVersionLabel;
    private String ebNewVersionDescription;
    private String ebEnvironmentName;


    public void execute() {
        // Setup our credentials
        BasicAWSCredentials credentials = new BasicAWSCredentials(awsAccessKey, aswSecretKey);

        // Setup clients
        AmazonS3 s3 = new AmazonS3Client(credentials);
        AWSElasticBeanstalk beanstalk = new AWSElasticBeanstalkClient(credentials);

        // First upload the war to S3
        s3.putObject(s3Bucket, s3Key, new File(war));

        // New version
        S3Location s3Location = new S3Location(s3Bucket, s3Key);
        CreateApplicationVersionRequest newEbVersion = new CreateApplicationVersionRequest(ebAppName, ebNewVersionLabel).
                withSourceBundle(s3Location).
                withDescription(ebNewVersionDescription).
                withAutoCreateApplication(false);
        beanstalk.createApplicationVersion(newEbVersion);

        // Update environment
        UpdateEnvironmentRequest ebEnv = new UpdateEnvironmentRequest().
                withEnvironmentName(ebEnvironmentName).
                withVersionLabel(ebNewVersionLabel);
        beanstalk.updateEnvironment(ebEnv);
    }
}
