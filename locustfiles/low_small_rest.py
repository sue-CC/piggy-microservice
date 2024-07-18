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

max_requests = {
    "create": 800,
    "get": 800,
    "update": 800
}
total_requests = {
    "create": 0,
    "get": 0,
    "update": 0
}
# Global maximum requests across all users
global_max_requests = 8


class AccountServiceTasks(SequentialTaskSet):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.locks = [Lock() for _ in range(3)]  # Create a list of 3 Lock objects
        self.created_usernames = []
        self.lock_indices = {"create": 0, "get": 1, "update": 2}  # Mapping for lock indices

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

    def make_request(self, endpoint, method="GET", payload=None):
        try:
            if method == "GET":
                response = self.client.get(endpoint)
            elif method == "POST":
                response = self.client.post(endpoint, json=payload)
            elif method == "PUT":
                response = self.client.put(endpoint, json=payload)
            else:
                logging.error(f"Unsupported HTTP method: {method}")
                return

            if response.status_code != 200:
                logging.error(f"Failed request: {response.status_code} {response.text}")

        except Exception as e:
            logging.error(f"Exception during request: {str(e)}")
            response = None

        return response

    @task
    def execute_tasks_in_sequence(self):
        self.create_account()
        self.update_account()
        self.get_account()

    def _increment_request_count(self, request_type):
        with self.locks[self.lock_indices[request_type]]:
            if total_requests[request_type] < global_max_requests:
                total_requests[request_type] += 1
                return True
            return False

    def create_account(self):
        if self._increment_request_count("create"):
            payload = self._generate_payload()
            self.created_usernames.append(payload["username"])
            response = self.make_request("/accounts/", method="POST", payload=payload)
            if response and response.status_code != 200:
                logging.error(f"Failed to create account: {response.status_code} {response.text}")

    def update_account(self):
        if self._increment_request_count("update"):
            username = self._generate_unique_username()
            self.created_usernames.append(username)
            payload = self.generate_update_account_payload()
            self.make_request(f"/accounts/{username}", method="PUT", payload=payload)

    def get_account(self):
        if self._increment_request_count("get") and self.created_usernames:
            username = random.choice(self.created_usernames)
            self.make_request(f"/accounts/{username}", method="GET")


class WebsiteUser(HttpUser):
    tasks = [AccountServiceTasks]
    wait_time = between(1, 3)
    request_counts = {"create": 0, "get": 0, "update": 0}
    start_time = None
    frequency_level = os.getenv('FREQUENCY_LEVEL', 'low')

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.stop_timer = None
        self.results = []
        self.max_requests = {
            "create": FREQUENCY_MAPPING.get(self.frequency_level, 8),
            "get": FREQUENCY_MAPPING.get(self.frequency_level, 8),
            "update": FREQUENCY_MAPPING.get(self.frequency_level, 8)
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
        if request_name in ["create", "get", "update"]:
            self.record_result(request_name, response_time, response.status_code)
            if total_requests[request_name] >= global_max_requests:
                self.stop()
                logging.info(f"Global limit reached for {request_name} requests. Stopping test.")


if __name__ == "__main__":
    import sys
    sys.argv = [sys.argv[0], "--host", "http://localhost:8080"]
    WebsiteUser().run()
