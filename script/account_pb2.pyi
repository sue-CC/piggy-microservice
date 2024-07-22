from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class Currency(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    USD: _ClassVar[Currency]
    EUR: _ClassVar[Currency]
    RUB: _ClassVar[Currency]

class TimePeriod(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    YEAR: _ClassVar[TimePeriod]
    QUARTER: _ClassVar[TimePeriod]
    MONTH: _ClassVar[TimePeriod]
    DAY: _ClassVar[TimePeriod]
    HOUR: _ClassVar[TimePeriod]

class StatisticMetric(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    INCOMES_AMOUNT: _ClassVar[StatisticMetric]
    EXPENSES_AMOUNT: _ClassVar[StatisticMetric]
    SAVING_AMOUNT: _ClassVar[StatisticMetric]
USD: Currency
EUR: Currency
RUB: Currency
YEAR: TimePeriod
QUARTER: TimePeriod
MONTH: TimePeriod
DAY: TimePeriod
HOUR: TimePeriod
INCOMES_AMOUNT: StatisticMetric
EXPENSES_AMOUNT: StatisticMetric
SAVING_AMOUNT: StatisticMetric

class Account(_message.Message):
    __slots__ = ("name", "incomes", "expenses", "saving", "note")
    NAME_FIELD_NUMBER: _ClassVar[int]
    INCOMES_FIELD_NUMBER: _ClassVar[int]
    EXPENSES_FIELD_NUMBER: _ClassVar[int]
    SAVING_FIELD_NUMBER: _ClassVar[int]
    NOTE_FIELD_NUMBER: _ClassVar[int]
    name: str
    incomes: _containers.RepeatedCompositeFieldContainer[Item]
    expenses: _containers.RepeatedCompositeFieldContainer[Item]
    saving: Saving
    note: str
    def __init__(self, name: _Optional[str] = ..., incomes: _Optional[_Iterable[_Union[Item, _Mapping]]] = ..., expenses: _Optional[_Iterable[_Union[Item, _Mapping]]] = ..., saving: _Optional[_Union[Saving, _Mapping]] = ..., note: _Optional[str] = ...) -> None: ...

class Item(_message.Message):
    __slots__ = ("title", "amount", "currency", "period")
    TITLE_FIELD_NUMBER: _ClassVar[int]
    AMOUNT_FIELD_NUMBER: _ClassVar[int]
    CURRENCY_FIELD_NUMBER: _ClassVar[int]
    PERIOD_FIELD_NUMBER: _ClassVar[int]
    title: str
    amount: str
    currency: Currency
    period: TimePeriod
    def __init__(self, title: _Optional[str] = ..., amount: _Optional[str] = ..., currency: _Optional[_Union[Currency, str]] = ..., period: _Optional[_Union[TimePeriod, str]] = ...) -> None: ...

class Saving(_message.Message):
    __slots__ = ("amount", "currency", "interest", "deposit", "capitalization")
    AMOUNT_FIELD_NUMBER: _ClassVar[int]
    CURRENCY_FIELD_NUMBER: _ClassVar[int]
    INTEREST_FIELD_NUMBER: _ClassVar[int]
    DEPOSIT_FIELD_NUMBER: _ClassVar[int]
    CAPITALIZATION_FIELD_NUMBER: _ClassVar[int]
    amount: str
    currency: Currency
    interest: str
    deposit: bool
    capitalization: bool
    def __init__(self, amount: _Optional[str] = ..., currency: _Optional[_Union[Currency, str]] = ..., interest: _Optional[str] = ..., deposit: bool = ..., capitalization: bool = ...) -> None: ...

class GetAccountRequest(_message.Message):
    __slots__ = ("name",)
    NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    def __init__(self, name: _Optional[str] = ...) -> None: ...

class GetAccountResponse(_message.Message):
    __slots__ = ("account",)
    ACCOUNT_FIELD_NUMBER: _ClassVar[int]
    account: Account
    def __init__(self, account: _Optional[_Union[Account, _Mapping]] = ...) -> None: ...

class SaveAccountRequest(_message.Message):
    __slots__ = ("accountName", "incomes", "expenses", "saving")
    ACCOUNTNAME_FIELD_NUMBER: _ClassVar[int]
    INCOMES_FIELD_NUMBER: _ClassVar[int]
    EXPENSES_FIELD_NUMBER: _ClassVar[int]
    SAVING_FIELD_NUMBER: _ClassVar[int]
    accountName: str
    incomes: _containers.RepeatedCompositeFieldContainer[Item]
    expenses: _containers.RepeatedCompositeFieldContainer[Item]
    saving: Saving
    def __init__(self, accountName: _Optional[str] = ..., incomes: _Optional[_Iterable[_Union[Item, _Mapping]]] = ..., expenses: _Optional[_Iterable[_Union[Item, _Mapping]]] = ..., saving: _Optional[_Union[Saving, _Mapping]] = ...) -> None: ...

class CreateAccountRequest(_message.Message):
    __slots__ = ("username", "password")
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    PASSWORD_FIELD_NUMBER: _ClassVar[int]
    username: str
    password: str
    def __init__(self, username: _Optional[str] = ..., password: _Optional[str] = ...) -> None: ...

class SuccessMessage(_message.Message):
    __slots__ = ("successMessage",)
    SUCCESSMESSAGE_FIELD_NUMBER: _ClassVar[int]
    successMessage: str
    def __init__(self, successMessage: _Optional[str] = ...) -> None: ...

class User(_message.Message):
    __slots__ = ("username", "password")
    USERNAME_FIELD_NUMBER: _ClassVar[int]
    PASSWORD_FIELD_NUMBER: _ClassVar[int]
    username: str
    password: str
    def __init__(self, username: _Optional[str] = ..., password: _Optional[str] = ...) -> None: ...

class UserRequest(_message.Message):
    __slots__ = ("user",)
    USER_FIELD_NUMBER: _ClassVar[int]
    user: User
    def __init__(self, user: _Optional[_Union[User, _Mapping]] = ...) -> None: ...

class UserResponse(_message.Message):
    __slots__ = ("message",)
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    message: str
    def __init__(self, message: _Optional[str] = ...) -> None: ...

class UserListResponse(_message.Message):
    __slots__ = ("users",)
    USERS_FIELD_NUMBER: _ClassVar[int]
    users: _containers.RepeatedScalarFieldContainer[str]
    def __init__(self, users: _Optional[_Iterable[str]] = ...) -> None: ...

class UpdateMessage(_message.Message):
    __slots__ = ("SuccessMessage",)
    SUCCESSMESSAGE_FIELD_NUMBER: _ClassVar[int]
    SuccessMessage: str
    def __init__(self, SuccessMessage: _Optional[str] = ...) -> None: ...

class AccountRequest(_message.Message):
    __slots__ = ("name",)
    NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    def __init__(self, name: _Optional[str] = ...) -> None: ...

class AccountStatisticsResponse(_message.Message):
    __slots__ = ("data_points",)
    DATA_POINTS_FIELD_NUMBER: _ClassVar[int]
    data_points: _containers.RepeatedCompositeFieldContainer[DataPoint]
    def __init__(self, data_points: _Optional[_Iterable[_Union[DataPoint, _Mapping]]] = ...) -> None: ...

class StatisticEntry(_message.Message):
    __slots__ = ("metric", "value")
    METRIC_FIELD_NUMBER: _ClassVar[int]
    VALUE_FIELD_NUMBER: _ClassVar[int]
    metric: StatisticMetric
    value: str
    def __init__(self, metric: _Optional[_Union[StatisticMetric, str]] = ..., value: _Optional[str] = ...) -> None: ...

class CurrencyEntry(_message.Message):
    __slots__ = ("currency", "rate")
    CURRENCY_FIELD_NUMBER: _ClassVar[int]
    RATE_FIELD_NUMBER: _ClassVar[int]
    currency: Currency
    rate: str
    def __init__(self, currency: _Optional[_Union[Currency, str]] = ..., rate: _Optional[str] = ...) -> None: ...

class DataPointId(_message.Message):
    __slots__ = ("account", "date")
    ACCOUNT_FIELD_NUMBER: _ClassVar[int]
    DATE_FIELD_NUMBER: _ClassVar[int]
    account: str
    date: str
    def __init__(self, account: _Optional[str] = ..., date: _Optional[str] = ...) -> None: ...

class DataPoint(_message.Message):
    __slots__ = ("id", "incomes", "expenses", "statistics", "rates")
    ID_FIELD_NUMBER: _ClassVar[int]
    INCOMES_FIELD_NUMBER: _ClassVar[int]
    EXPENSES_FIELD_NUMBER: _ClassVar[int]
    STATISTICS_FIELD_NUMBER: _ClassVar[int]
    RATES_FIELD_NUMBER: _ClassVar[int]
    id: DataPointId
    incomes: _containers.RepeatedCompositeFieldContainer[Item]
    expenses: _containers.RepeatedCompositeFieldContainer[Item]
    statistics: _containers.RepeatedCompositeFieldContainer[StatisticEntry]
    rates: _containers.RepeatedCompositeFieldContainer[CurrencyEntry]
    def __init__(self, id: _Optional[_Union[DataPointId, _Mapping]] = ..., incomes: _Optional[_Iterable[_Union[Item, _Mapping]]] = ..., expenses: _Optional[_Iterable[_Union[Item, _Mapping]]] = ..., statistics: _Optional[_Iterable[_Union[StatisticEntry, _Mapping]]] = ..., rates: _Optional[_Iterable[_Union[CurrencyEntry, _Mapping]]] = ...) -> None: ...

class AccountStatistics(_message.Message):
    __slots__ = ("incomes", "expenses", "saving")
    INCOMES_FIELD_NUMBER: _ClassVar[int]
    EXPENSES_FIELD_NUMBER: _ClassVar[int]
    SAVING_FIELD_NUMBER: _ClassVar[int]
    incomes: _containers.RepeatedCompositeFieldContainer[Item]
    expenses: _containers.RepeatedCompositeFieldContainer[Item]
    saving: Saving
    def __init__(self, incomes: _Optional[_Iterable[_Union[Item, _Mapping]]] = ..., expenses: _Optional[_Iterable[_Union[Item, _Mapping]]] = ..., saving: _Optional[_Union[Saving, _Mapping]] = ...) -> None: ...

class AccountS(_message.Message):
    __slots__ = ("incomes", "expenses", "saving")
    INCOMES_FIELD_NUMBER: _ClassVar[int]
    EXPENSES_FIELD_NUMBER: _ClassVar[int]
    SAVING_FIELD_NUMBER: _ClassVar[int]
    incomes: _containers.RepeatedCompositeFieldContainer[Item]
    expenses: _containers.RepeatedCompositeFieldContainer[Item]
    saving: Saving
    def __init__(self, incomes: _Optional[_Iterable[_Union[Item, _Mapping]]] = ..., expenses: _Optional[_Iterable[_Union[Item, _Mapping]]] = ..., saving: _Optional[_Union[Saving, _Mapping]] = ...) -> None: ...

class UpdateAccountRequest(_message.Message):
    __slots__ = ("name", "account")
    NAME_FIELD_NUMBER: _ClassVar[int]
    ACCOUNT_FIELD_NUMBER: _ClassVar[int]
    name: str
    account: AccountS
    def __init__(self, name: _Optional[str] = ..., account: _Optional[_Union[AccountS, _Mapping]] = ...) -> None: ...

class Empty(_message.Message):
    __slots__ = ()
    def __init__(self) -> None: ...

class UpdateAccountResponse(_message.Message):
    __slots__ = ("message",)
    MESSAGE_FIELD_NUMBER: _ClassVar[int]
    message: str
    def __init__(self, message: _Optional[str] = ...) -> None: ...
