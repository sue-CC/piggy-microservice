from locust import User
from locust.exception import LocustError

import time
from typing import Any, Callable

import grpc
import grpc.experimental.gevent as grpc_gevent
from grpc_interceptor import ClientInterceptor

import account_pb2_grpc
import notification_pb2_grpc

grpc_gevent.init_gevent()


class LocustInterceptor(ClientInterceptor):
    def __init__(self, environment, *args, **kwargs):
        super().__init__(*args, **kwargs)

        self.env = environment

    def intercept(
            self,
            method: Callable,
            request_or_iterator: Any,
            call_details: grpc.ClientCallDetails,
    ):
        response = None
        exception = None
        start_perf_counter = time.perf_counter()
        response_length = 0
        try:
            response = method(request_or_iterator, call_details)
            response_length = response.result().ByteSize()
        except grpc.RpcError as e:
            exception = e

        self.env.events.request.fire(
            request_type="grpc",
            name=call_details.method,
            response_time=(time.perf_counter() - start_perf_counter) * 1000,
            response_length=response_length,
            response=response,
            context=None,
            exception=exception,
        )
        return response


class GrpcUser(User):
    abstract = True
    hosts = {"account": "0.0.0.0:9090",
             "auth": "0.0.0.0:9091",
             "recipient": "0.0.0.0:9092",
             "statistics": "0.0.0.0:9093",
             }

    def __init__(self, environment):
        super().__init__(environment)
        if not self.hosts:
            raise LocustError("You must specify at least one host for each task.")

        self.current_task = None
        self._channel = None
        self.stub = None

    def set_host_for_task(self, task_name):
        self.current_task = task_name
        if task_name in self.hosts:
            host = self.hosts[task_name]
            self._channel = grpc.insecure_channel(host)
            interceptor = LocustInterceptor(environment=self.environment)
            self._channel = grpc.intercept_channel(self._channel, interceptor)

            # Dynamically set the stub_class based on task_name
            self.stub_class = self._get_stub_class(task_name)
            if self.stub_class is None:
                raise LocustError(f"No stub class specified for task {task_name}")

            self.stub = self.stub_class(self._channel)
        else:
            raise LocustError(f"No host specified for task {task_name}")

    def _get_stub_class(self, task_name):
        stub_classes = {
            "account": account_pb2_grpc.AccountServiceStub,
            "auth": account_pb2_grpc.UserServiceStub,
            "recipient": notification_pb2_grpc.NotificationServiceStub,
            "statistics": account_pb2_grpc.StatisticsServiceStub
        }
        return stub_classes.get(task_name, None)

