/* Copyright (c) 2014-2020 Julien Rialland <julien.rialland@gmail.com>
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
package com.github.jrialland.ajpclient.pool;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.buffer.Unpooled;

public class Buffers {

	private static final ByteBufAllocator allocator = new PooledByteBufAllocator(true); 
	
	public static ByteBuf makeBuffer(int size) {
		return allocator.buffer(size);
	}

	public static ByteBuf wrap(byte[] array, int offset, int len) {
		return Unpooled.wrappedBuffer(array, offset, len);
	}
}
