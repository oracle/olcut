## OLCUT Protobuf support

This project provides support for reading & writing configuration objects in protobuf format.

It also provides a protobuf serialization format for marshalled provenance objects.

Note: the tests use a mixture of pb and pbtxt files. Some tests use both, but the tests with invalid
configurations use pbtxt as it's easier to construct a malformed pbtxt in a text editor.