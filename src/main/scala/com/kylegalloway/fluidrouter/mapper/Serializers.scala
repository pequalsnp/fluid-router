package com.kylegalloway.fluidrouter.mapper

import java.time.ZonedDateTime

import com.esotericsoftware.kryo.io.{Input, Output}
import com.esotericsoftware.kryo.{Kryo, Serializer}

class ZonedDateTimeSerializer extends Serializer[ZonedDateTime] {
  override def read(kryo: Kryo, input: Input, `type`: Class[ZonedDateTime]): ZonedDateTime = {
    ZonedDateTime.parse(input.readString())
  }

  override def write(kryo: Kryo, output: Output, `object`: ZonedDateTime): Unit = {
    output.writeString(`object`.toString)
  }
}
