import boto3
from botocore.exceptions import NoCredentialsError, PartialCredentialsError, ClientError

def validate_aws_credentials(access_key, secret_key):
    try:
        # Initialize STS client
        client = boto3.client(
            'sts',
            aws_access_key_id=access_key,
            aws_secret_access_key=secret_key
        )
        # Call GetCallerIdentity
        response = client.get_caller_identity()
        print("Credentials are valid!")
        print(f"UserId: {response['UserId']}")
        print(f"Account: {response['Account']}")
        print(f"ARN: {response['Arn']}")
    except NoCredentialsError:
        print("No credentials found.")
    except PartialCredentialsError:
        print("Incomplete credentials provided.")
    except ClientError as e:
        print(f"Error: {e}")

# Replace with your actual access key and secret key
validate_aws_credentials('<YourAccessKey>', '<YourSecretKey>')
