#include <signal.h>
#include <stdio.h>
#include <stdint.h>
#include <string.h>
#include <unistd.h>
#include <mosquitto.h>
#include <wiringPi.h>
#include <softTone.h>

#define mqtt_host "localhost"
#define mqtt_port 1883
#define MQTT_TOPIC "house/myroom/alarm"
#define OFF 0
#define ON 1
#define LIGHT 12
#define BUZZER 16

static int run = 1;
int LedFlag = false;
int LedData = OFF;
const char* MegON = "ON";
const char* MegOFF = "OFF";

void handle_signal(int s)
{
	run = 0;
}
void connect_callback(struct mosquitto* mosq, void* obj, int result)
{
	printf("connect callback, rc=%d\n", result);
}
void message_callback(struct mosquitto* mosq, void* obj, const struct mosquitto_message* message)
{
	printf("got message '%.*s' for topic '%s'\n", message->payloadlen, (char*)message->payload, message->topic);
	if (strcmp(MegON, (char*)message->payload) == 0)
	{
		LedFlag = true;
		LedData = ON;
		printf("LED ON\n");
	}
	else if (strcmp(MegOFF, (char*)message->payload) == 0)
	{
		LedFlag = true;
		LedData = OFF;
		printf("LED OFF\n");
	}
	else
	{
		printf("LED not\n");
	}
}

int main(int argc, char* argv[])
{
	uint8_t reconnect = true;
	struct mosquitto* mosq;
	int rc = 0;
	int input;
	signal(SIGINT, handle_signal);
	signal(SIGTERM, handle_signal);
	mosquitto_lib_init();
	wiringPiSetupGpio();
	pinMode(LIGHT,INPUT);
	pinMode(BUZZER, OUTPUT);
	softToneCreate(BUZZER);
	mosq = mosquitto_new(NULL, true, 0);

	if (mosq) {
		mosquitto_connect_callback_set(mosq, connect_callback);
		mosquitto_message_callback_set(mosq, message_callback);
		rc = mosquitto_connect(mosq, mqtt_host, mqtt_port, 60);
		mosquitto_subscribe(mosq, NULL, MQTT_TOPIC, 0);

		while (run)
		{
			rc = mosquitto_loop(mosq, -1, 1);
			if (run && rc) {
				printf("connection error!\n");
				sleep(10);
				mosquitto_reconnect(mosq);
			}
			inout = digitalRead(LIGHT);
			if (LedData==ON) {
				if(input==0)
					softToneWrite(BUZZER, 329);
				else
					softToneWrite(BUZZER, 0);
			}
			else {
				softToneWrite(BUZZER, 0);
			}

		}
		mosquitto_destroy(mosq);
	}
	mosquitto_lib_cleanup();
	return rc;
}