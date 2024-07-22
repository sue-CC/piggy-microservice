import os
import logging
import random
import time
import csv
import grpc
from locust import User, between, events
from locust_grpc import GRPCUser, grpc_task
import your_service_pb2_grpc

logging.basicConfig(level=logging.INFO)

# Define environment variables
FREQUENCY_LEVEL = os.getenv('FREQUENCY_LEVEL', 'medium')
SIZE_LEVEL = os.getenv('SIZE_LEVEL', 'small')

# Set mappings
FREQUENCY_MAPPING = {'low': 8, 'medium': 80, 'high': 800}
SIZE_MAPPING = {'small': 50, 'medium': 1000, 'large': 500000}

max_requests = {"create_account": 800,
                "get_account": 800,
                "update_account": 800,
                "get_auth": 800,
                "update_auth": 800,
                "post_auth": 800,
                "get_statistics": 800,
                "update_statistics": 800,
                "get_recipient": 800,
                "update_recipient": 800
                }
total_requests = {"create_account": 0,
                  "get_account": 0,
                  "update_account": 0,
                  "get_auth": 0,
                  "update_auth": 0,
                  "post_auth": 0,
                  "get_statistics": 0,
                  "update_statistics": 0,
                  "get_recipient": 0,
                  "update_recipient": 0
                  }
# Global maximum requests across all users
global_max_requests = 80

class AccountServiceTasks(GRPCUser):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.locks = [Lock() for _ in range(10)]
        self.created_usernames = []
        self.created_users = []
        self.lock_indices = {
            "create_account": 0,
            "get_account": 1,
            "update_account": 2,
            "get_auth": 3,
            "update_auth": 4,
            "post_auth": 5,
            "get_statistics": 6,
            "update_statistics": 7,
            "get_recipient": 8,
            "update_recipient": 9,
        }  # Mapping for lock indices

        # gRPC channel and stub
        self.channel = grpc.insecure_channel('localhost:50051')
        self.stub = your_service_pb2_grpc.YourServiceStub(self.channel)  # Replace with your generated stub class

    def _generate_unique_username(self):
        while True:
            username = f"{random.randint(0, 999999):06}"
            if username not in self.created_usernames:
                self.created_usernames.append(username)
                return username

    def _generate_payload(self):
        username = self._generate_unique_username()
        payload = {
            "username": username,
            "password": "1234",
        }
        return payload

    def _generate_create_user_payload(self):
        username = self._generate_unique_username()
        payload = {
            "username": username,
            "password": "1234",
        }
        return payload

    @grpc_task
    def create_account(self):
        if self._increment_request_count("create_account"):
            payload = self._generate_payload()
            request = your_service_pb2.CreateAccountRequest(**payload)  # Replace with your request message
            response = self.stub.CreateAccount(request)
            if response.status_code != 200:
                logging.error(f"Failed to create account: {response.status_code} {response.message}")

    @grpc_task
    def update_account(self):
        if self._increment_request_count("update_account"):
            username = f"{random.randint(0, 999999):06}"
            payload = self.generate_update_account_payload()
            request = your_service_pb2.UpdateAccountRequest(username=username, **payload)  # Replace with your request message
            self.stub.UpdateAccount(request)

    @grpc_task
    def get_account(self):
        if self._increment_request_count("get_account") and self.created_usernames:
            username = random.choice(self.created_usernames)
            request = your_service_pb2.GetAccountRequest(username=username)  # Replace with your request message
            self.stub.GetAccount(request)

    @grpc_task
    def update_statistics(self):
        if self._increment_request_count("update_statistics") and self.created_usernames:
            username = random.choice(self.created_usernames)
            payload = self.generate_update_statistics_payload()
            request = your_service_pb2.UpdateStatisticsRequest(username=username, **payload)  # Replace with your request message
            self.stub.UpdateStatistics(request)

    @grpc_task
    def get_statistics(self):
        if self._increment_request_count("get_statistics") and self.created_usernames:
            username = random.choice(self.created_usernames)
            request = your_service_pb2.GetStatisticsRequest(username=username)  # Replace with your request message
            self.stub.GetStatistics(request)

    @grpc_task
    def get_recipient(self):
        if self._increment_request_count("get_recipient") and self.created_usernames:
            username = random.choice(self.created_usernames)
            request = your_service_pb2.GetRecipientRequest(username=username)  # Replace with your request message
            self.stub.GetRecipient(request)

    @grpc_task
    def update_recipient(self):
        if self._increment_request_count("update_recipient"):
            username = random.choice(self.created_usernames)
            payload = self.generate_update_recipient_payload()
            request = your_service_pb2.UpdateRecipientRequest(username=username, **payload)  # Replace with your request message
            self.stub.UpdateRecipient(request)

    @grpc_task
    def create_user(self):
        if self._increment_request_count("post_auth"):
            payload = self._generate_create_user_payload()
            request = your_service_pb2.CreateUserRequest(**payload)  # Replace with your request message
            self.stub.CreateUser(request)

    @grpc_task
    def get_users(self):
        if self._increment_request_count("get_auth"):
            self.stub.GetUsers(your_service_pb2.GetUsersRequest())  # Replace with your request message

    @grpc_task
    def update_user(self):
        if self._increment_request_count("update_auth"):
            username = random.choice(self.created_users)
            payload = {
                "username": username,
                "password": "1234",
            }
            request = your_service_pb2.UpdateUserRequest(**payload)  # Replace with your request message
            self.stub.UpdateUser(request)

class WebsiteUser(User):
    tasks = [AccountServiceTasks]
    wait_time = between(1, 3)
    request_counts = {"create_account": 0,
                      "get_account": 0,
                      "update_account": 0,
                      "get_auth": 0,
                      "update_auth": 0,
                      "post_auth": 0,
                      "get_statistics": 0,
                      "update_statistics": 0,
                      "get_recipient": 0,
                      "update_recipient": 0
                      }
    start_time = None
    frequency_level = os.getenv('FREQUENCY_LEVEL', 'low')

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.stop_timer = None
        self.results = []
        self.max_requests = {
            "create_account": FREQUENCY_MAPPING.get(self.frequency_level, 8),
            "get_account": FREQUENCY_MAPPING.get(self.frequency_level, 8),
            "update_account": FREQUENCY_MAPPING.get(self.frequency_level, 8),
            "get_auth": FREQUENCY_MAPPING.get(self.frequency_level, 8),
            "update_auth": FREQUENCY_MAPPING.get(self.frequency_level, 8),
            "post_auth": FREQUENCY_MAPPING.get(self.frequency_level, 8),
            "get_statistics": FREQUENCY_MAPPING.get(self.frequency_level, 8),
            "update_statistics": FREQUENCY_MAPPING.get(self.frequency_level, 8),
            "get_recipient": FREQUENCY_MAPPING.get(self.frequency_level, 8),
            "update_recipient": FREQUENCY_MAPPING.get(self.frequency_level, 8),
        }

    def on_start(self):
        self.client.verify = False
        self.results = []
        self.start_time = time.time()
        self.stop_timer = Timer(120, self.stop_user)
        self.stop_timer.start()
        events.request.add_listener(self.on_request_handler)

    def on_stop(self):
        if self.stop_timer and self.stop_timer.is_alive():
            self.stop_timer.cancel()
            self.stop_timer.join()
        self.export_results_to_csv()

    def stop_user(self):
        self.environment.runner.quit()
        logging.info("Test stopped after 2 minutes")

    def export_results_to_csv(self):
        with open('low_small_grpc_results.csv', 'w', newline='') as csvfile:
            fieldnames = ['Task', 'Count']
            writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
            writer.writeheader()
            for task, count in self.request_counts.items():
                writer.writerow({'Task': task, 'Count': count})

    def on_request_handler(self, request_type, name, response_time, response_length, exception, context):
        self.request_counts[name] += 1
