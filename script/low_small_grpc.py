import csv
import logging
import random
import time
import os

from threading import Lock, Timer
import grpc_user
import notification_pb2
from grpc_user import GrpcUser
from locust import between, events, task
import account_pb2

logging.basicConfig(level=logging.INFO)

# Define environment variables
FREQUENCY_LEVEL = os.getenv('FREQUENCY_LEVEL', 'low')
SIZE_LEVEL = os.getenv('SIZE_LEVEL', 'small')

# Set mappings
FREQUENCY_MAPPING = {'low': 8, 'medium': 80, 'high': 800}
SIZE_MAPPING = {'small': 50, 'medium': 1000, 'large': 500000}

global_max_requests = 80

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


class AccountServiceTasks(grpc_user.GrpcUser):
    wait_time = between(1, 3)

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.request_counts = None
        self.results = None
        self.locks = {name: Lock() for name in [
            "create_account", "get_account", "update_account",
            "get_auth", "update_auth", "post_auth",
            "get_statistics", "update_statistics",
            "get_recipient", "update_recipient"
        ]}
        self.created_usernames = ['Tom1', 'Tom2', 'Tom3', 'Tom4']
        self.created_users = ['Tom1', 'Tom2', 'Tom3', 'Tom4']
        self.create_recipients = ['Tom1', 'Tom2', 'Tom3', 'Tom4']

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

    def _generate_unique_recipients(self):
        while True:
            username = f"{random.randint(0, 999999):06}"
            if username not in self.create_recipients:
                self.create_recipients.append(username)
                return username

    # @task
    # def execute_tasks_in_sequence(self):
    #     self.create_account()
    #     self.get_account()
    #     self.update_account()
    #     self.update_statistics()
    #     self.get_statistics()
    #     self.update_recipient()
    #     self.get_recipient()
    #     # self.create_user()
    #     # self.get_users()
    #     # self.update_user()

    @task
    def create_account(self):
        self.set_host_for_task("account")
        if self._increment_request_count("create_account"):
            username = self._generate_unique_username()
            password = "1234"
            request = account_pb2.CreateAccountRequest(username=username, password=password)
            try:
                self.stub.CreateNewAccount(request)
                self.created_usernames.append(username)
            except Exception as e:
                logging.error(f"Failed to create account: {e}")

    @task
    def update_account(self):
        self.set_host_for_task("account")
        if self._increment_request_count("update_account") and self.created_usernames:
            username = self._generate_unique_username()
            request = account_pb2.SaveAccountRequest(
                accountName=username,
                incomes=[account_pb2.Item(title="Salary", amount="5000", currency=account_pb2.USD,
                                          period=account_pb2.MONTH)],
                expenses=[
                    account_pb2.Item(title="Rent", amount="1200", currency=account_pb2.USD, period=account_pb2.MONTH)],
                saving=account_pb2.Saving(amount="1000", currency=account_pb2.USD, interest="0.05", deposit=True,
                                          capitalization=False)
            )
            try:
                self.stub.SaveCurrentAccount(request)
                self.created_usernames.append(username)
            except Exception as e:
                logging.error(f"Failed to update account: {e}")

    @task
    def get_account(self):
        self.set_host_for_task("account")
        if self._increment_request_count("get_account") and self.created_usernames:
            username = random.choice(self.created_usernames)
            request = account_pb2.GetAccountRequest(name=username)
            try:
                self.stub.GetAccountByName(request)
            except Exception as e:
                logging.error(f"Failed to get account: {e}")

    @task
    def update_statistics(self):
        self.set_host_for_task("statistics")
        if self._increment_request_count("update_statistics") and self.created_usernames:
            username = random.choice(self.created_usernames)
            request = account_pb2.UpdateAccountRequest(
                name=username,
                account=account_pb2.AccountS(
                    incomes=[
                        account_pb2.Item(title="Salary",
                                         amount="3000",
                                         currency=account_pb2.USD,
                                         period=account_pb2.MONTH
                                         )
                    ],
                    expenses=[
                        account_pb2.Item(title="Groceries",
                                         amount="2000",
                                         currency=account_pb2.USD,
                                         period=account_pb2.MONTH
                                         ),
                        account_pb2.Item(title="Car",
                                         amount="600",
                                         currency=account_pb2.USD,
                                         period=account_pb2.YEAR
                                         )
                    ],
                    saving=account_pb2.Saving(
                        amount="20000",
                        currency=account_pb2.RUB,
                        interest="2.6",
                        deposit=True,
                        capitalization=False
                    )
                )
            )
            try:
                self.stub.UpdateAccountStatistics(request)
            except Exception as e:
                logging.error(f"Failed to update account: {e}")

    @task
    def get_statistics(self):
        self.set_host_for_task("statistics")
        if self._increment_request_count("get_statistics"):
            username = random.choice(self.created_usernames)
            request = account_pb2.AccountRequest(name=username)
            self.stub.GetCurrentAccountStatistics(request)


    @task
    def update_recipient(self):
        self.set_host_for_task("recipient")
        if self._increment_request_count("update_recipient") and self.created_usernames:
            username = self._generate_unique_username()
            settings_1 = notification_pb2.NotificationSettings(
                active=True,
                frequency=notification_pb2.Frequency.HIGH,
            )
            settings_2 = notification_pb2.NotificationSettings(
                active=False,
                frequency=notification_pb2.Frequency.MEDIUM,
            )

            entry_1 = notification_pb2.NotificationEntry(
                type=notification_pb2.NotificationType.REMIND,
                settings=settings_1
            )
            entry_2 = notification_pb2.NotificationEntry(
                type=notification_pb2.NotificationType.BACKUP,
                settings=settings_2
            )

            # Construct Recipient instance
            recipient = notification_pb2.Recipient(
                accountName=username,
                email=f"{username}@gmail.com",
                scheduledNotifications=[entry_1, entry_2]
            )
            request = notification_pb2.UpdateRecipientRequest(
                name=username,
                recipient=recipient
            )
            self.create_recipients.append(username)
            self.stub.UpdateRecipient(request)

    @task
    def get_recipient(self):
        self.set_host_for_task("recipient")
        if self._increment_request_count("get_recipient") and self.created_usernames:
            username = random.choice(self.create_recipients)
            request = notification_pb2.GetRecipientRequest(name=username)
            self.stub.GetRecipient(request)

    @task
    def create_user(self):
        self.set_host_for_task("auth")
        if self._increment_request_count("post_auth"):
            username = self._generate_unique_users()
            password = "1234"
            user = account_pb2.User(username=username, password=password)
            request = account_pb2.UserRequest(user=user)
            try:
                self.stub.AddUser(request)
                self.created_users.append(username)
            except Exception as e:
                logging.error(f"Failed to create user: {e}")

    @task
    def get_users(self):
        self.set_host_for_task("auth")
        if self._increment_request_count("get_auth"):
            request = account_pb2.Empty()
            self.stub.GetUsers(request=request)

    @task
    def update_user(self):
        self.set_host_for_task("auth")
        if self._increment_request_count("update_auth") and self.created_users:
            username = self._generate_unique_users()
            password = "1234"
            user = account_pb2.User(username=username, password=password)
            request = account_pb2.UserRequest(user=user)
            try:
                self.stub.UpdateUser(request)
                self.created_users.append(username)
            except Exception as e:
                logging.error(f"Failed to create user: {e}")


class WebsiteUser(GrpcUser):

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
        if request_name in [max_requests.keys()]:
            self.record_result(request_name, response_time, response.status_code)
            if total_requests[request_name] >= global_max_requests:
                self.stop()
                logging.info(f"Global limit reached for {request_name} requests. Stopping test.")


if __name__ == "__main__":
    import sys

    sys.argv = [sys.argv[0], "--host", "http://localhost:8080"]
    WebsiteUser().run()
