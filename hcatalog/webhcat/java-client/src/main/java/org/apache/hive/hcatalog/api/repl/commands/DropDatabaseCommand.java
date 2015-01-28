/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.hive.hcatalog.api.repl.commands;

import org.apache.hadoop.conf.Configuration;
import org.apache.hive.hcatalog.api.HCatClient;
import org.apache.hive.hcatalog.common.HCatException;
import org.apache.hive.hcatalog.data.ReaderWriter;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DropDatabaseCommand extends HiveCommand {
  private String dbName = null;
  private long eventId;

 public DropDatabaseCommand(String dbName, long eventId) {
    this.dbName = dbName;
    this.eventId = eventId;
  }

  public DropDatabaseCommand(){
    // trivial ctor to support Writable reflections instantiation
    // do not expect to use this object as-is, unless you call
    // readFields after using this ctor
  }

   @Override
  public List<String> get() {
    // DROP (DATABASE|SCHEMA) [IF EXISTS] database_name [RESTRICT|CASCADE];
    StringBuilder sb = new StringBuilder();
    sb.append("DROP DATABASE IF EXISTS ");
    sb.append(dbName);
    sb.append(" CASCADE");
    return Arrays.asList(sb.toString());
  }

  @Override
  public boolean isRetriable() {
    return true;
  }

  @Override
  public boolean isUndoable() {
    return false;
  }

  @Override
  public List<String> getUndo() {
    throw new UnsupportedOperationException("getUndo called on command that does returned false for isUndoable");
  }

  @Override
  public List<String> cleanupLocationsPerRetry() {
    return new ArrayList<String>();
  }

  @Override
  public List<String> cleanupLocationsAfterEvent() {
    return new ArrayList<String>();
  }

  @Override
  public long getEventId() {
    return eventId;
  }

  @Override
  public void write(DataOutput dataOutput) throws IOException {
    ReaderWriter.writeDatum(dataOutput, dbName);
    ReaderWriter.writeDatum(dataOutput, Long.valueOf(eventId));
  }

  @Override
  public void readFields(DataInput dataInput) throws IOException {
    dbName = (String)ReaderWriter.readDatum(dataInput);
    eventId = ((Long)ReaderWriter.readDatum(dataInput)).longValue();
  }

  @Override
  void run(HCatClient client, Configuration conf) throws HCatException {
    client.dropDatabase(this.dbName,true, HCatClient.DropDBMode.CASCADE);
  }

  @Override
  boolean isRunnableFromHCatClient() {
    return true;
  }
}
