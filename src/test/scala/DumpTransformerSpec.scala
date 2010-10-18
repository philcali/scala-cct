package com.philipcali.cct
package test

import dump.DumpTransformer
import java.io.File

class DumpTransformerSpec extends TransformerSpec {
  val transformer = DumpTransformer(working)

  "Dump Transformer" should "produce a replicate dir tree" in {
    new File(working + "_dump") should be ('exists)
    new File(working + "_dump/syllabus_dir") should be ('exists)
    new File(working + "_dump/important_dir") should be ('exists)
  }
}
