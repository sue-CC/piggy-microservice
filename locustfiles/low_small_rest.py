import os
import logging
import random
import time
import csv
from locust import HttpUser, SequentialTaskSet, task, between, events
from threading import Lock, Timer

logging.basicConfig(level=logging.INFO)

# Define environment variables
FREQUENCY_LEVEL = os.getenv('FREQUENCY_LEVEL', 'high')
SIZE_LEVEL = os.getenv('SIZE_LEVEL', 'small')

# Set mappings
FREQUENCY_MAPPING = {'low': 8, 'medium': 80, 'high': 800}
SIZE_MAPPING = {'small': 50, 'medium': 1000, 'large': 500000}

max_requests = {"create_account": 800,
                "get_account": 800,
                "update_account": 800,
                "get_auth": 800,
                "delete_auth": 800,
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
                  "delete_auth": 0,
                  "post_auth": 0,
                  "get_statistics": 0,
                  "update_statistics": 0,
                  "get_recipient": 0,
                  "update_recipient": 0
                  }
# Global maximum requests across all users
global_max_requests = 400


class AccountServiceTasks(SequentialTaskSet):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.locks = [Lock() for _ in range(10)]
        self.created_usernames = []
        self.lock_indices = {
            "create_account": 0,
            "get_account": 1,
            "update_account": 2,
            "get_auth": 3,
            "delete_auth": 4,
            "post_auth": 5,
            "get_statistics": 6,
            "update_statistics": 7,
            "get_recipient": 8,
            "update_recipient": 9,
        }  # Mapping for lock indices

    @staticmethod
    def generate_update_account_payload():
        payload = {
            "incomes": [],
            "expenses": [],
            "saving": {
                "amount": 0,
                "currency": "EUR",
                "interest": 0,
                "deposit": "true",
                "capitalization": "false"
            }
        }
        return payload

    @staticmethod
    def generate_update_statistics_payload():
        payload = {
            "incomes": [
            ],
            "expenses": [
            ],
            "saving": {
                "amount": 0,
                "currency": "USD",
                "interest": 0,
                "deposit": "true",
                "capitalization": "false"
            }
        }
        return payload

    @staticmethod
    def generate_update_recipient_payload():
        payload = {
            "accountName": "",
            "email": "",
            "scheduledNotifications": {
                "BACKUP": {
                    "active": "true",
                    "frequency": "HIGH",
                    "lastNotified": ""
                },
                "REMIND": {
                    "active": "false",
                    "frequency": "LOW",
                    "lastNotified": ""
                }
            }
        }
        return payload

    def _generate_unique_username(self):
        while True:
            username = f"{random.randint(0, 999999):06}"
            self.created_usernames.append(username)
            return username

    def _generate_payload(self):
        username = self._generate_unique_username()
        payload = {
            "username": username,
            "password": "1234",
        }
        return payload

    def make_request(self, endpoint, method="GET", payload=None, port=8080):
        try:
            url = f"http://0.0.0.0:{port}{endpoint}"
            if method == "GET":
                response = self.client.get(url)
            elif method == "POST":
                response = self.client.post(url, json=payload)
            elif method == "PUT":
                response = self.client.put(url, json=payload)
            elif method == "DELETE":
                response = self.client.delete(url, json=payload)
            else:
                logging.error(f"Unsupported HTTP method: {method}")
                return

            # if response.status_code[0] != 2:
            #     logging.error(f"Failed request: {response.status_code} {response.text}")

        except Exception as e:
            logging.error(f"Exception during request: {str(e)}")
            response = None

        return response

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
        self.get_users()
        self.delete_user()

    def _increment_request_count(self, request_type):
        with self.locks[self.lock_indices[request_type]]:
            if total_requests[request_type] < global_max_requests:
                total_requests[request_type] += 1
                return True
            return False

    def create_account(self):
        if self._increment_request_count("create_account"):
            payload = self._generate_payload()
            response = self.make_request("/accounts/", method="POST", payload=payload, port=6000)
            if response and response.status_code != 200:
                logging.error(f"Failed to create account: {response.status_code} {response.text}")

    def update_account(self):
        if self._increment_request_count("update_account"):
            username = f"{random.randint(0, 999999):06}"
            self.created_usernames.append(username)
            payload = self.generate_update_account_payload()
            self.make_request(f"/accounts/{username}", method="PUT", payload=payload, port=6000)

    def get_account(self):
        if self._increment_request_count("get_account") and self.created_usernames:
            username = random.choice(self.created_usernames)
            self.make_request(f"/accounts/{username}", method="GET", port=6000)

    def update_statistics(self):
        if self._increment_request_count("update_statistics") and self.created_usernames:
            username = random.choice(self.created_usernames)
            payload = self.generate_update_statistics_payload()
            self.make_request(f"/statistics/{username}", method="PUT", payload=payload, port=7000)

    def get_statistics(self):
        if self._increment_request_count("get_statistics") and self.created_usernames:
            username = random.choice(self.created_usernames)
            self.make_request(f"/statistics/{username}", method="GET", port=7000)

    def get_recipient(self):
        if self._increment_request_count("get_recipient") and self.created_usernames:
            username = random.choice(self.created_usernames)
            self.make_request(f"/recipients/{username}", method="GET", port=8083)

    def update_recipient(self):
        if self._increment_request_count("update_recipient"):
            username = random.choice(self.created_usernames)
            payload = self.generate_update_recipient_payload()
            self.make_request(f"/recipients/{username}", method="PUT", payload=payload, port=8083)

    def create_user(self):
        if self._increment_request_count("post_auth"):
            payload = self._generate_payload()
            self.make_request("/users", method="POST", payload=payload, port=5000)

    def get_users(self):
        if self._increment_request_count("get_auth"):
            self.make_request("/users", method="GET", port=5000)

    def delete_user(self):
        if self._increment_request_count("delete_auth"):
            username = random.choice(self.created_usernames)
            payload = {
                "username": username,
                "password": "1234",
            }
            response = self.make_request("/users", method="DELETE", payload=payload, port=5000)
            self.created_usernames.remove(username)
            if response.status_code != 200:
                logging.error(f"Failed to delete account: {response.status_code} {response.text}")


class WebsiteUser(HttpUser):
    tasks = [AccountServiceTasks]
    wait_time = between(1, 3)
    request_counts = {"create_account": 0,
                      "get_account": 0,
                      "update_account": 0,
                      "get_auth": 0,
                      "delete_auth": 0,
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
            "delete_auth": FREQUENCY_MAPPING.get(self.frequency_level, 8),
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
        with open('low_small_rest.csv', mode='w', newline='') as file:
            writer = csv.writer(file)
            writer.writerow(['Request Name', 'Response Time (ms)', 'Response Code', 'Frequency Level'])
            for result in self.results:
                writer.writerow(result)

    def record_result(self, request_name, response_time, response_code):
        self.results.append([request_name, response_time, response_code, self.frequency_level])
        self.request_counts[request_name] += 1
        if self.request_counts[request_name] >= self.max_requests[request_name]:
            self.stop()
            logging.info(f"All {request_name} requests completed. Stopping test.")

    def on_request_handler(self, request_type, name, response_time, response_length, response, **kwargs):
        request_name = name.split("/")[-1]
        if request_name in ["create_account",
                            "get_account",
                            "update_account",
                            "get_auth",
                            "delete_auth",
                            "post_auth",
                            "get_statistics",
                            "update_statistics",
                            "get_recipient",
                            "update_recipient"]:
            self.record_result(request_name, response_time, response.status_code)
            if total_requests[request_name] >= global_max_requests:
                self.stop()
                logging.info(f"Global limit reached for {request_name} requests. Stopping test.")


if __name__ == "__main__":
    import sys

    sys.argv = [sys.argv[0], "--host", "http://localhost:8080"]
    WebsiteUser().run()
