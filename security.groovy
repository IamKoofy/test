import boto3
from botocore.exceptions import NoCredentialsError, EndpointConnectionError

# Configure AWS SDK with the region
region = 'us-east-1'  # Set your region here

# Optionally, set your credentials directly
aws_access_key = 'YOUR_ACCESS_KEY'
aws_secret_key = 'YOUR_SECRET_KEY'
aws_session_token = 'YOUR_SESSION_TOKEN'  # Only required for temporary credentials

# Initialize a session using your AWS credentials
session = boto3.Session(
    aws_access_key_id=aws_access_key,
    aws_secret_access_key=aws_secret_key,
    aws_session_token=aws_session_token,  # Comment out if not using temporary credentials
    region_name=region
)

# Create an ELBv2 client
elb_client = session.client('elbv2')

try:
    # Call the ELBv2 API to describe load balancers
    response = elb_client.describe_load_balancers()
    
    # Print out the load balancer details
    for lb in response['LoadBalancers']:
        print(f"Name: {lb['LoadBalancerName']}")
        print(f"DNS Name: {lb['DNSName']}")
        print(f"Type: {lb['Type']}")
        print(f"State: {lb['State']['Code']}")
        print("")

except NoCredentialsError:
    print("Credentials not found. Please check your AWS credentials.")
except EndpointConnectionError as e:
    print(f"Error connecting to the AWS endpoint: {e}")
except Exception as e:
    print(f"An error occurred: {e}")
