package com.linkbi.datax.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class HbaseReaderDto implements Serializable {

  private String readerMaxVersion;

  private String readerMode;

  private Range readerRange;

}

