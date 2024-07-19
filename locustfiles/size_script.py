from locust import HttpUser, SequentialTaskSet, task, between
import os
import random
import string
import logging

logging.basicConfig(level=logging.INFO)


class AccountServiceTasks(SequentialTaskSet):
    def __init__(self, *args, **kwargs):
        super().__init__(args, kwargs)
        self.example_username = None
        self.username_length = None
        self.payload_size = None

    def on_start(self):
        self.client.base_url = "http://account-service-rest:6000"

        # set request size/length
        self.payload_size = int(os.getenv('PAYLOAD_SIZE', 500000))  # 默认大小为500,000字节（500KB）
        self.username_length = int(os.getenv('USERNAME_LENGTH', 12))  # 默认用户名长度为12

        # generate example_username
        self.example_username = self._generate_username()

    @task
    def create_account(self):
        self._make_request("/accounts/", method="POST")

    @task
    def get_account(self):
        self._make_request(f"/accounts/{self.example_username}", method="GET")

    @task
    def update_account(self):
        self._make_request(f"/accounts/{self.example_username}", method="PUT")

    def _generate_username(self):
        # 生成指定长度的用户名，包含字母和数字
        characters = string.ascii_letters + string.digits
        username = ''.join(random.choices(characters, k=self.username_length))
        return username

    def _generate_payload(self):
        # generate payload
        random_string = ''.join(random.choices(string.ascii_letters + string.digits, k=self.payload_size))
        payload = {
            "username": self._generate_username(),
            "password": "1",  # fixed password
            "random_field": random_string
        }
        return payload

    def _make_request(self, endpoint, method="GET"):
        payload = None
        if method in ["POST", "PUT"]:
            payload = self._generate_payload()

        for _ in range(3):
            if method == "GET":
                response = self.client.get(endpoint)
            elif method == "POST":
                response = self.client.post(endpoint, json=payload)
            elif method == "PUT":
                response = self.client.put(endpoint, json=payload)
            else:
                logging.error(f"Unsupported HTTP method: {method}")
                return

            logging.info(f"Request URL: {self.client.base_url}{endpoint}")
            logging.info(f"Response code: {response.status_code}")
            logging.info(f"Response content: {response.text}")
            if response.status_code != 200:
                logging.error(f"Failed request: {response.status_code} {response.text}")


class StatisticsServiceTasks(SequentialTaskSet):

    def on_start(self):
        self.client.base_url = "http://statistics-service-rest:7000"

    @task
    def update_statistics(self):
        self._make_request("/statistics/")

    @task
    def get_statistics(self):
        self._make_request("/statistics/")

    def _make_request(self, endpoint):
        for _ in range(2):
            response = self.client.get(endpoint)
            logging.info(f"Request URL: {self.client.base_url}{endpoint}")
            logging.info(f"Response code: {response.status_code}")
            logging.info(f"Response content: {response.text}")
            if response.status_code != 200:
                logging.error(f"Failed request: {response.status_code} {response.text}")


class NotificationServiceTasks(SequentialTaskSet):

    def on_start(self):
        self.client.base_url = "http://notification-service-rest:8083"

    @task
    def update_notification_setting(self):
        self._make_request("/recipients/")

    @task
    def get_notification_setting(self):
        self._make_request("/recipients/")

    def _make_request(self, endpoint):
        for _ in range(2):
            response = self.client.get(endpoint)
            logging.info(f"Request URL: {self.client.base_url}{endpoint}")
            logging.info(f"Response code: {response.status_code}")
            logging.info(f"Response content: {response.text}")
            if response.status_code != 200:
                logging.error(f"Failed request: {response.status_code} {response.text}")


class AuthServiceTasks(SequentialTaskSet):

    def on_start(self):
        self.client.base_url = "http://auth-service-rest:5000"

    @task
    def get_users(self):
        self._make_request("/users")

    @task
    def create_user(self):
        self._make_request("/users")

    @task
    def delete_user(self):
        self._make_request("/users")

    def _make_request(self, endpoint):
        for _ in range(3):
            response = self.client.get(endpoint)
            logging.info(f"Request URL: {self.client.base_url}{endpoint}")
            logging.info(f"Response code: {response.status_code}")
            logging.info(f"Response content: {response.text}")
            if response.status_code != 200 or 201:
                logging.error(f"Failed request: {response.status_code} {response.text}")


class WebsiteUser(HttpUser):
    tasks = {AuthServiceTasks, AccountServiceTasks, StatisticsServiceTasks, NotificationServiceTasks}
    wait_time = between(1, 3)

    def on_start(self):
        self.client.verify = False

