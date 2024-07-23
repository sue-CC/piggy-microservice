import uuid
from locust import HttpUser, SequentialTaskSet, task, between, events
import os
import logging
import random
import secrets
import time
from threading import Lock, Timer

logging.basicConfig(level=logging.INFO)

# Define environment variables
FREQUENCY_LEVEL = os.getenv('FREQUENCY_LEVEL', 'low')
SIZE_LEVEL = os.getenv('SIZE_LEVEL', 'small')

# Set mappings
FREQUENCY_MAPPING = {'low': 8, 'medium': 80, 'high': 400}
SIZE_MAPPING = {'small': 50, 'medium': 1000, 'large': 100000}

global_max_requests = FREQUENCY_MAPPING[FREQUENCY_LEVEL]
payload_size = SIZE_MAPPING[SIZE_LEVEL]

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
total_requests = {name: 0 for name in max_requests.keys()}


class AccountServiceTasks(SequentialTaskSet):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.request_counts = None
        self.locks = {name: Lock() for name in [
            "create_account", "get_account", "update_account",
            "get_auth", "update_auth", "post_auth",
            "get_statistics", "update_statistics",
            "get_recipient", "update_recipient"
        ]}
        self.created_usernames = ['Tom111']
        self.created_users = ['Tom111']
        self.create_recipients = ['Tom111']

    @staticmethod
    def generate_update_account_payload():
        payload = {
            "incomes": [
                {
                    "title": "Salary",
                    "amount": "0",
                    "currency": "EUR",
                    "period": "MONTH"
                }
            ],
            "expenses": [
                {
                    "title": "Rent",
                    "amount": "20",
                    "currency": "EUR",
                    "period": "MONTH"
                }
            ],
            "saving": {
                "amount": "30",
                "currency": "EUR",
                "interest": "2.5",
                "deposit": "true",
                "capitalization": "false"
            }
        }
        return payload

    @staticmethod
    def generate_update_statistics_payload():
        payload = {
            "incomes": [
                {
                    "title": "Rent",
                    "amount": "1",
                    "currency": "EUR",
                    "period": "MONTH"
                }
            ],
            "expenses": [
                {
                    "title": "Rent",
                    "amount": "1",
                    "currency": "EUR",
                    "period": "MONTH"
                }
            ],
            "saving": {
                "amount": "2",
                "currency": "USD",
                "interest": "2.5",
                "deposit": "true",
                "capitalization": "false"
            }
        }
        return payload

    @staticmethod
    def generate_update_recipient_payload(accountName):
        payload = {
            "accountName": accountName,
            "email": "1234@gmail.com",
            "scheduledNotifications": {
                "BACKUP": {
                    "active": "true",
                    "frequency": "HIGH",
                },
                "REMIND": {
                    "active": "false",
                    "frequency": "LOW",
                }
            }
        }
        return payload

    def _increment_request_count(self, task_name):
        with self.locks[task_name]:
            if total_requests[task_name] < global_max_requests:
                total_requests[task_name] += 1
                return True
            return False

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

    def _generate_unique_users(self):
        while True:
            username = f"{random.randint(0, 999999):06}"
            if username not in self.created_users:
                self.created_users.append(username)
                return username

    def _generate_create_user_payload(self):
        username = self._generate_unique_users()
        payload = {
            "username": username,
            "password": "1234",
        }
        return payload

    def _generate_unique_recipients(self):
        while True:
            username = f"{random.randint(0, 999999):06}"
            if username not in self.create_recipients:
                self.create_recipients.append(username)
                return username

    def make_request(self, endpoint, method="GET", payload=None, port=8080):
        try:
            url = f"http://145.108.225.14:{port}{endpoint}"
            request_size = len(str(payload).encode('utf-8')) if payload else 0
            if method == "GET":
                response = self.client.get(url)
            elif method == "POST":
                response = self.client.post(url, json=payload)
            elif method == "PUT":
                response = self.client.put(url, json=payload)
            elif method == "DELETE":
                response = self.client.delete(url)
            else:
                logging.error(f"Unsupported HTTP method: {method}")
                return

            if response:
                self.environment.events.request.fire(
                    request_id = str(uuid.uuid4())
                    request_type="REST"+method,
                    name=endpoint+request_id,
                    response_time=response.elapsed.total_seconds() * 1000,
                    response_length=request_size,
                    response=response,
                    context={},  
                    exception=None,
                    start_time=time.time(),
                    # request_size=request_size
                )

        except Exception as e:
            logging.error(f"Exception during request: {str(e)}")
            response = None

        return response

    @task
    def execute_tasks_in_sequence(self):
        self.create_account()
        self.get_account()
        self.update_account()
        self.update_statistics()
        self.get_statistics()
        self.update_recipient()
        self.get_recipient()
        self.create_user()
        self.update_user()

    def create_account(self):
        with self.locks["create_account"]:
            if total_requests["create_account"] < global_max_requests * 2:
                total_requests["create_account"] += 1
                username = f"{random.randint(0, 999999):06}"
                password = secrets.token_bytes(20).hex()
                payload = {
                    "username": username,
                    "password": password,
                }
                response = self.make_request("/accounts/", method="POST", payload=payload, port=6000)
                if response and response.status_code != 200:
                    logging.error(f"Failed to create account: {response.status_code} {response.text}")
                else:
                    self.created_usernames.append(payload["username"])  # Ensure usernames are added

    def update_account(self):
        if self._increment_request_count("update_account"):
            username = f"{random.randint(0, 999999):06}"
            self.created_usernames.append(username)
            payload = self.generate_update_account_payload()
            self.make_request(f"/accounts/{username}", method="PUT", payload=payload, port=6000)

    def get_account(self):
        if self._increment_request_count("get_account"):
            username = "Tom111"
            self.make_request(f"/accounts/{username}", method="GET", port=6000)

    def update_recipient(self):
        if self._increment_request_count("update_recipient"):
            username = self._generate_unique_username()
            payload = self.generate_update_recipient_payload(username)
            self.make_request(f"/recipients/{username}", method="PUT", payload=payload, port=8083)

    def get_recipient(self):
        if self._increment_request_count("get_recipient"):
            username = random.choice(self.created_usernames)
            self.make_request(f"/recipients/{username}", method="GET", port=8083)

    def update_statistics(self):
        if self._increment_request_count("update_statistics"):
            username = random.choice(self.created_usernames)
            payload = self.generate_update_statistics_payload()
            self.make_request(f"/statistics/{username}", method="PUT", payload=payload, port=7000)

    def get_statistics(self):
        if self._increment_request_count("get_statistics"):
            username = random.choice(self.created_usernames)
            self.make_request(f"/statistics/{username}", method="GET", port=7000)

    def create_user(self):
        if self._increment_request_count("post_auth"):
            payload = self._generate_create_user_payload()
            self.make_request(f"/auth/", method="POST", payload=payload, port=5000)

    def update_user(self):
        if self._increment_request_count("update_auth"):
            username = "Tom111"
            payload = self._generate_create_user_payload()
            self.make_request(f"/auth/{username}", method="PUT", payload=payload, port=5000)


class WebsiteUser(HttpUser):
    tasks = [AccountServiceTasks]
    wait_time = between(1, 3)
    request_counts = { name: 0 
        "create_account": 0,
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
        events.request.remove_listener(self.on_request_handler)

    def stop_user(self):
        self.environment.runner.quit()
        logging.info("Test stopped after 2 minutes")

    def record_result(self, request_name, response_time, response_code, request_size):
        self.results.append([request_name, response_time, response_code, self.frequency_level, request_size])
        self.request_counts[request_name] += 1
        if self.request_counts[request_name] >= self.max_requests[request_name]:
            self.stop()

    def on_request_handler(self, request_type, name, response_time, response_length, response, context, exception, start_time, request_size, **kwargs):
        request_name = name.split("/")[-1]
        if request_name in self.request_counts:
            self.record_result(request_name, response_time, response.status_code, request_size)
            if total_requests[request_name] >= global_max_requests:
                self.stop()
