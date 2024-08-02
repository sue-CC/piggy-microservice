import secrets

import logging
import random
import time
import os

from threading import Lock, Timer

import rest_user
from rest_user import RestUser
from locust import between, task

logging.basicConfig(level=logging.INFO)

# Define environment variables
FREQUENCY_LEVEL = os.getenv('FREQUENCY_LEVEL', 'low')
SIZE_LEVEL = os.getenv('SIZE_LEVEL', 'small')

# Set mappings
FREQUENCY_MAPPING = {'low': 8, 'medium': 80, 'high': 400}
SIZE_MAPPING = {'small': 50, 'medium': 1000, 'large': 100000}

global_max_requests = FREQUENCY_MAPPING[FREQUENCY_LEVEL]
payload_size = SIZE_MAPPING[SIZE_LEVEL]

max_requests = {"create_account": 400,
                "get_account": 400,
                "update_account": 400,
                "get_auth": 400,
                "update_auth": 400,
                "post_auth": 400,
                "get_statistics": 400,
                "update_statistics": 400,
                "get_recipient": 400,
                "update_recipient": 400
                }
total_requests = {name: 0 for name in max_requests.keys()}


class AccountServiceTasks(rest_user.RestUser):
    wait_time = between(1, 3)

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

    def _generate_unique_users(self):
        while True:
            username = f"{random.randint(0, 999999):06}"
            if username not in self.created_users:
                self.created_users.append(username)
                return username

    @task
    def execute_tasks_in_sequence(self):
        self.create_account()
        self.update_account()
        self.get_account()
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
                password = secrets.token_bytes(6).hex()
                payload = {
                    "username": username,
                    "password": password
                }
                try:
                    self.make_request("/accounts/", "POST", payload=payload, task_name="account")
                    self.created_usernames.append(username)
                except Exception as e:
                    logging.error(f"Failed to create account: {e}")

    def update_account(self):
        if self._increment_request_count("update_account"):
            username = f"{random.randint(0, 999999):06}"
            payload = {
                "incomes": ["123"], "expenses": [],
                "saving": {}
            }
            try:
                self.make_request(f"/accounts/{username}", method="PUT", payload=payload, task_name="account")
                self.created_usernames.append(username)
            except Exception as e:
                logging.error(f"Failed to update account: {e}")

    def get_account(self):
        if self._increment_request_count("get_account") and self.created_usernames:
            username = random.choice(self.created_usernames)
            try:
                self.make_request(f"/accounts/{username}", method="GET", task_name="account")
            except Exception as e:
                logging.error(f"Failed to get account: {e}")

    def update_statistics(self):
        if self._increment_request_count("update_statistics"):
            try:
                self.make_request(f"/statistics/{random.choice(self.created_usernames)}",
                                  method="PUT",
                                  payload={
                                      "incomes": ["123"], "expenses": [],
                                      "saving": {}
                                  },
                                  task_name="statistics"
                                  )
            except Exception as e:
                logging.error(f"Failed to update statistics: {e}")

    def get_statistics(self):
        if self._increment_request_count("get_statistics"):
            self.make_request(f"/statistics/{random.choice(self.created_usernames)}", payload=None, method="GET", task_name="statistics")

    def update_recipient(self):
        if self._increment_request_count("update_recipient"):
            username = f"{random.randint(0, 999999):06}"
            payload = {
                "accountName": username,
                "email": "12@gmail.com"
            }
            self.make_request(f"/recipients/{username}", method="PUT", payload=payload, task_name="recipient")

    def get_recipient(self):
        if self._increment_request_count("get_recipient"):
            username = "Tom111"
            self.make_request(f"/recipients/{username}", method="GET", port=8083, task_name="recipient")

    def create_user(self):
        if self._increment_request_count("post_auth"):
            username = f"{random.randint(0, 999999):06}"
            password = secrets.token_bytes(6).hex()
            payload = {
                "username": username,
                "password": password
            }
            self.make_request(f"/users", method="POST", payload=payload, port=5000, task_name="auth")

    def update_user(self):
        if self._increment_request_count("update_auth"):
            username = f"{random.randint(0, 999999):06}"
            password = secrets.token_bytes(6).hex()
            payload = {
                "username": username,
                "password": password
            }
            self.make_request(f"/users", method="PUT", payload=payload, port=5000, task_name="auth")


class WebsiteUser(RestUser):
    tasks = [AccountServiceTasks]
    wait_time = between(1, 3)
    total_requests = {name: 0 for name in max_requests.keys()}

    start_time = None
    frequency_level = os.getenv('FREQUENCY_LEVEL', 'low')

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.stop_timer = None
        self.results = []

    def on_start(self):
        self.results = []
        self.start_time = time.time()
        self.stop_timer = Timer(150, self.stop_user)

        self.stop_timer.start()

    def on_stop(self):
        if self.stop_timer and self.stop_timer.is_alive():
            self.stop_timer.cancel()
            self.stop_timer.join()

    def stop_user(self):
        self.environment.runner.quit()
        logging.info("Test stopped after 2 min 30s")
