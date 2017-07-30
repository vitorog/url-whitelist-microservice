# Useful script that tests insertion and validation requests and checks if the responses
# correctly match the requests.
# Assumes that the application and RabbitMQ server are running with the default configurations.
import pika
import json
import unittest
import logging
import sys


class ApplicationTest(unittest.TestCase):

    def setUp(self):
        self.validation_requests = {}
        self.expected_match = {}
        self.channel = None
        self.validation_requests_count = 0
        self.responses_received_count = 0
        self.insertion_queue_name = 'insertion.queue'
        self.validation_queue_name = 'validation.queue'
        self.response_exchange_name = 'response.exchange'
        self.response_exchange_routing_key_name = 'response.routing.key'
        self.response_queue_name = 'response.queue'
        self.rabbitmq_server = 'localhost'
        self.build_validation_requests()

    def build_validation_requests(self):
        self.validation_requests[0] = '{"client":"google","url":"www.google.com", "correlationId":0}'
        self.expected_match[0] = True

        self.validation_requests[1] = '{"client":"google","url":"www.google.com.br", "correlationId":1}'
        self.expected_match[1] = True

        self.validation_requests[2] = '{"client":"microsoft","url":"www.microsoft.com", "correlationId":2}'
        self.expected_match[2] = True

        self.validation_requests[3] = '{"client":"apple","url":"apple.com", "correlationId":3}'
        self.expected_match[3] = True

        self.validation_requests[4] = '{"client":"yahoo","url":"yahoo.com.br", "correlationId":4}'
        self.expected_match[4] = True

        self.validation_requests[5] = '{"client":"google","url":"localhost", "correlationId":5}'
        self.expected_match[5] = True

        self.validation_requests[6] = '{"client":"google","url":"localhost:8080", "correlationId":6}'
        self.expected_match[6] = True

        self.validation_requests[7] = '{"client":"google","url":"google_fake.com", "correlationId":7}'
        self.expected_match[7] = False

        self.validation_requests[8] = '{"client":"microsoft","url":"www.microsoft2.com", "correlationId":8}'
        self.expected_match[8] = False

    def send_request(self, request, queue_name):
        self.channel.basic_publish(exchange='',
                                   routing_key=queue_name,
                                   body=json.dumps(request),
                                   properties=pika.BasicProperties(
                                       content_type='application/json'
                                   ))

    def insert_entries(self):
        self.send_request('{"client":"google", "regex":"(www.)?(google)(\\\\.\\\\D+){1,2}"}', self.insertion_queue_name)
        self.send_request('{"client":"microsoft", "regex":"(www.)?(microsoft)(\\\\.\\\\D+){1,2}"}', self.insertion_queue_name)
        self.send_request('{"client":"yahoo", "regex":"(www.)?(yahoo)(\\\\.\\\\D+){1,2}"}', self.insertion_queue_name)
        self.send_request('{"client":"apple", "regex":"(www.)?(apple)(\\\\.\\\\D+){1,2}"}', self.insertion_queue_name)
        self.send_request('{"client":null, "regex":"(localhost)(:\\\\d+)?"}', self.insertion_queue_name)
        self.send_request('{"client":null, "regex":"localhost"}', self.insertion_queue_name)

    def validate_entries(self):
        self.validation_requests_count = len(self.validation_requests.keys())

        for i in range(0, self.validation_requests_count):
            self.send_request(self.validation_requests[i], self.validation_queue_name)

    def on_response(self, ch, method, properties, body):
        log = logging.getLogger("ApplicationTest")

        data = json.loads(body)
        correlation_id = data["correlationId"]
        match = data["match"]
        regex = data["regex"]
        if regex is None:
            regex = "null"

        log.info(str(match) + " - " + regex + " - " + self.validation_requests[correlation_id])
        self.assertEquals(match, self.expected_match[correlation_id])

        self.responses_received_count = self.responses_received_count + 1
        if self.responses_received_count == self.validation_requests_count:
            self.channel.stop_consuming()

    def test_application(self):
        print("Creating connection...")
        connection = pika.BlockingConnection(pika.ConnectionParameters(self.rabbitmq_server))
        self.channel = connection.channel()

        print("Inserting entities...")
        self.insert_entries()

        print("Requesting URLs validation...")
        self.channel.queue_declare(queue=self.response_queue_name)

        self.channel.queue_bind(exchange=self.response_exchange_name,
                                queue=self.response_queue_name,
                                routing_key=self.response_exchange_routing_key_name)

        self.validate_entries()
        self.channel.basic_consume(self.on_response,
                                   queue=self.response_queue_name,
                                   no_ack=True)
        self.channel.start_consuming()
        connection.close()


if __name__ == "__main__":
    if sys.version_info >= (3,0):
        print("Currently this script only works with Python 2.x... :(")
        print("Exiting now.")
        sys.exit(1)
    logging.basicConfig(stream=sys.stderr)
    logging.getLogger("ApplicationTest").setLevel(logging.INFO)
    unittest.main()
