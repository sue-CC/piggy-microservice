import secrets

import logging
import random
import time
import os

from threading import Lock, Timer
import grpc_user
import notification_pb2
from grpc_user import GrpcUser
from locust import between, task
import account_pb2

logging.basicConfig(level=logging.INFO)

# Define environment variables
FREQUENCY_LEVEL = os.getenv('FREQUENCY_LEVEL', 'high')
SIZE_LEVEL = os.getenv('SIZE_LEVEL', 'medium')

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


class AccountServiceTasks(grpc_user.GrpcUser):
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
        self.create_recipients = ['Tom111']

    def _increment_request_count(self, task_name):
        with self.locks[task_name]:
            if total_requests[task_name] < global_max_requests:
                total_requests[task_name] += 1
                return True
            return False

    def _generate_unique_username(self):
        while True:
            username = f"{random.randint(0, 999999):05}"
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
            username = f"{random.randint(0, 999999):05}"
            if username not in self.create_recipients:
                self.create_recipients.append(username)
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
        self.set_host_for_task("account")
        with self.locks["create_account"]:
            if total_requests["create_account"] < global_max_requests*2:
                total_requests["create_account"] += 1
                username = f"{random.randint(0, 99999):05}"
                password = secrets.token_bytes(495).hex()
                request = account_pb2.CreateAccountRequest(username=username, password=password)
                try:
                    self.stub.CreateNewAccount(request)
                    self.created_usernames.append(username)
                except Exception as e:
                    logging.error(f"Failed to create account: {e}")

    def update_account(self):
        self.set_host_for_task("account")
        if self._increment_request_count("update_account"):
            username = f"{random.randint(0, 999999):06}"
            amount = secrets.token_bytes(476).hex()
            request = account_pb2.SaveAccountRequest(
                accountName=username,
                incomes=[account_pb2.Item(title=amount, amount="2000", currency=account_pb2.USD,
                                          period=account_pb2.MONTH)],
                expenses=[
                    account_pb2.Item(title="Rent", amount="20", currency=account_pb2.USD, period=account_pb2.MONTH)],
                saving=account_pb2.Saving(amount="30", currency=account_pb2.USD, interest="25", deposit=True,
                                          capitalization=False)
            )
            try:
                self.stub.SaveCurrentAccount(request)
                self.created_usernames.append(username)
            except Exception as e:
                logging.error(f"Failed to update account: {e}")

    def get_account(self):
        self.set_host_for_task("account")
        if self._increment_request_count("get_account") and self.created_usernames:
            username = "Tom111"
            request = account_pb2.GetAccountRequest(name=username)
            try:
                self.stub.GetAccountByName(request)
            except Exception as e:
                logging.error(f"Failed to get account: {e}")

    def update_statistics(self):
        self.set_host_for_task("statistics")
        if self._increment_request_count("update_statistics") and self.created_usernames:
            username = f"{random.randint(0, 999999):06}"
            amount = secrets.token_bytes(475).hex()
            request = account_pb2.UpdateAccountRequest(
                name=username,
                account=account_pb2.AccountS(
                    incomes=[
                        account_pb2.Item(title=amount,
                                         amount="10",
                                         currency=account_pb2.USD,
                                         period=account_pb2.MONTH
                                         )
                    ],
                    expenses=[
                        account_pb2.Item(title="Rent",
                                         amount="1",
                                         currency=account_pb2.USD,
                                         period=account_pb2.MONTH
                                         )
                    ],
                    saving=account_pb2.Saving(
                        amount="2",
                        currency=account_pb2.RUB,
                        interest="2.5",
                        deposit=True,
                        capitalization=False
                    )
                )
            )
            try:
                self.stub.UpdateAccountStatistics(request)
            except Exception as e:
                logging.error(f"Failed to update account: {e}")

    def get_statistics(self):
        self.set_host_for_task("statistics")
        if self._increment_request_count("get_statistics"):
            request = account_pb2.AccountRequest(name="Tom111")
            self.stub.GetCurrentAccountStatistics(request)

    def update_recipient(self):
        self.set_host_for_task("recipient")
        if self._increment_request_count("update_recipient") and self.created_usernames:
            username = f"{random.randint(0, 999999):06}"
            amount = secrets.token_bytes(481).hex()
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

            recipient = notification_pb2.Recipient(
                accountName=username,
                email=amount,
                scheduledNotifications=[entry_1, entry_2]
            )
            request = notification_pb2.UpdateRecipientRequest(
                name=username,
                recipient=recipient
            )
            self.stub.UpdateRecipient(request)

    def get_recipient(self):
        self.set_host_for_task("recipient")
        if self._increment_request_count("get_recipient"):
            request = notification_pb2.GetRecipientRequest(name="Tom111")
            self.stub.GetRecipient(request)

    def create_user(self):
        self.set_host_for_task("auth")
        if self._increment_request_count("post_auth"):
            username = f"{random.randint(0, 999999):06}"
            password = secrets.token_bytes(493).hex()
            user = account_pb2.User(username=username, password=password)
            request = account_pb2.UserRequest(user=user)
            try:
                self.stub.AddUser(request)
            except Exception as e:
                logging.error(f"Failed to create user: {e}")

    def update_user(self):
        self.set_host_for_task("auth")
        if self._increment_request_count("update_auth") and self.created_users:
            username = f"{random.randint(0, 999999):06}"
            password = secrets.token_bytes(493).hex()
            user = account_pb2.User(username=username, password=password)
            request = account_pb2.UserRequest(user=user)
            try:
                self.stub.UpdateUser(request)
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
        self.stop_timer = Timer(150, self.stop_user)

        self.stop_timer.start()

    def on_stop(self):
        if self.stop_timer and self.stop_timer.is_alive():
            self.stop_timer.cancel()
            self.stop_timer.join()

    def stop_user(self):
        self.environment.runner.quit()
        logging.info("Test stopped after 2 min 30s")