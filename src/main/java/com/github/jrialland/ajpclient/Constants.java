/* Copyright (c) 2014-2022 Julien Rialland <julien.rialland@gmail.com>
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *     http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package com.github.jrialland.ajpclient;

/**
 * useful constants about the ajp13 protocol
 *
 * @author Julien Rialland <julien.rialland@gmail.com>
 *
 */
public interface Constants {

	int CPING_TIMEOUT_SECONDS = 5;

	int MAX_MESSAGE_SIZE = 8 * 1024;// 8k

	int MAX_RECEIVE_CHUNK_SIZE = MAX_MESSAGE_SIZE - 5; // (5
																			// =
																			// magic(2bytes)
																			// +
																			// packet
																			// length(2bytes)
																			// +
																			// prefix
																			// (1byte))

	int MAX_SEND_CHUNK_SIZE = MAX_MESSAGE_SIZE - 6; // (6 =
																		// magic(2bytes)
																		// +
																		// packet
																		// length(2bytes)
																		// +
																		// chunk
																		// length(2bytes)
																		// )

	byte[] CLIENT_MAGIC = new byte[] { 0x12, 0x34 };

	byte[] CONTAINER_MAGIC = new byte[] { 0x41, 0x42 };

	int REQUEST_TERMINATOR = 0xff;

	byte PREFIX_FORWARD_REQUEST = 0x02;

	byte PREFIX_CPING = 0x0a;

	byte PREFIX_CPONG = 9;

	byte PREFIX_SEND_BODY_CHUNK = 3;

	byte PREFIX_SEND_HEADERS = 4;

	byte PREFIX_END_RESPONSE = 5;

	byte PREFIX_GET_BODY_CHUNK = 6;

	byte[] CPING_MESSAGE = new byte[] { CLIENT_MAGIC[0], CLIENT_MAGIC[1], 0x00, 0x01, PREFIX_CPING };
}
