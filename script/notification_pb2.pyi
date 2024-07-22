from google.protobuf.internal import containers as _containers
from google.protobuf.internal import enum_type_wrapper as _enum_type_wrapper
from google.protobuf import descriptor as _descriptor
from google.protobuf import message as _message
from typing import ClassVar as _ClassVar, Iterable as _Iterable, Mapping as _Mapping, Optional as _Optional, Union as _Union

DESCRIPTOR: _descriptor.FileDescriptor

class Frequency(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    LOW: _ClassVar[Frequency]
    MEDIUM: _ClassVar[Frequency]
    HIGH: _ClassVar[Frequency]

class NotificationType(int, metaclass=_enum_type_wrapper.EnumTypeWrapper):
    __slots__ = ()
    BACKUP: _ClassVar[NotificationType]
    REMIND: _ClassVar[NotificationType]
LOW: Frequency
MEDIUM: Frequency
HIGH: Frequency
BACKUP: NotificationType
REMIND: NotificationType

class NotificationSettings(_message.Message):
    __slots__ = ("active", "frequency")
    ACTIVE_FIELD_NUMBER: _ClassVar[int]
    FREQUENCY_FIELD_NUMBER: _ClassVar[int]
    active: bool
    frequency: Frequency
    def __init__(self, active: bool = ..., frequency: _Optional[_Union[Frequency, str]] = ...) -> None: ...

class NotificationEntry(_message.Message):
    __slots__ = ("type", "settings")
    TYPE_FIELD_NUMBER: _ClassVar[int]
    SETTINGS_FIELD_NUMBER: _ClassVar[int]
    type: NotificationType
    settings: NotificationSettings
    def __init__(self, type: _Optional[_Union[NotificationType, str]] = ..., settings: _Optional[_Union[NotificationSettings, _Mapping]] = ...) -> None: ...

class Recipient(_message.Message):
    __slots__ = ("accountName", "email", "scheduledNotifications")
    ACCOUNTNAME_FIELD_NUMBER: _ClassVar[int]
    EMAIL_FIELD_NUMBER: _ClassVar[int]
    SCHEDULEDNOTIFICATIONS_FIELD_NUMBER: _ClassVar[int]
    accountName: str
    email: str
    scheduledNotifications: _containers.RepeatedCompositeFieldContainer[NotificationEntry]
    def __init__(self, accountName: _Optional[str] = ..., email: _Optional[str] = ..., scheduledNotifications: _Optional[_Iterable[_Union[NotificationEntry, _Mapping]]] = ...) -> None: ...

class GetRecipientRequest(_message.Message):
    __slots__ = ("name",)
    NAME_FIELD_NUMBER: _ClassVar[int]
    name: str
    def __init__(self, name: _Optional[str] = ...) -> None: ...

class UpdateRecipientRequest(_message.Message):
    __slots__ = ("name", "recipient")
    NAME_FIELD_NUMBER: _ClassVar[int]
    RECIPIENT_FIELD_NUMBER: _ClassVar[int]
    name: str
    recipient: Recipient
    def __init__(self, name: _Optional[str] = ..., recipient: _Optional[_Union[Recipient, _Mapping]] = ...) -> None: ...

class RecipientResponse(_message.Message):
    __slots__ = ("recipient",)
    RECIPIENT_FIELD_NUMBER: _ClassVar[int]
    recipient: Recipient
    def __init__(self, recipient: _Optional[_Union[Recipient, _Mapping]] = ...) -> None: ...
