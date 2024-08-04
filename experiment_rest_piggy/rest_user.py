import logging
import time
import uuid
import requests
from locust import User
from locust.exception import LocustError
from typing import Any, Callable

class RestInterceptor:
    def __init__(self, environment, *args, **kwargs):
        self.env = environment

    def intercept(self, method: Callable, method_name: str, endpoint: str, url: str, **kwargs: Any):
        request_id = str(uuid.uuid4())
        response = None
        exception = None
        start_perf_counter = time.perf_counter()
        response_length = 0
        request_size = 0

        try:
            # Calculate the size of the request payload
            request_data = kwargs.get("data") or kwargs.get("json")
            if request_data:
                request_size = len(request_data.encode('utf-8')) if isinstance(request_data, str) else len(str(request_data).encode('utf-8'))

            response = method(url, **kwargs)
            response_length = len(response.content)
        except requests.RequestException as e:
            exception = e
            response = e.response

        response_time = (time.perf_counter() - start_perf_counter) * 1000

        self.env.events.request.fire(
            request_type="http",
            name=f"{method_name} {endpoint} {request_id}",
            response_time=response_time,
            response_length=response_length,
            context=None,
            exception=exception,
            # request_size=request_size
        )

        return response

class RestUser(User):
    abstract = True
    hosts = {
        "account": 6000,
        "auth": 5000,
        "recipient": 8083,
        "statistics": 7000,
    }

    def __init__(self, environment):
        super().__init__(environment)
        self.rest_interceptor = RestInterceptor(environment)

    def make_request(self, endpoint, method="GET", payload=None, task_name=None, port=None):
        if task_name:
            port = self.hosts.get(task_name, port)
        if not port:
            raise LocustError(f"No port specified for task {task_name} and no port provided.")

        url = f"http://0.0.0.0:{port}{endpoint}"
        response = None
        try:
            method_func = self._get_method_func(method)
            if method_func:
                response = self.rest_interceptor.intercept(method_func, method, endpoint, url, json=payload)
        except Exception as e:
            logging.error(f"Exception during request: {str(e)}")

        return response

    def _get_method_func(self, method):
        method_map = {
            "GET": requests.get,
            "POST": requests.post,
            "PUT": requests.put,
            "DELETE": requests.delete
        }
        return method_map.get(method)
