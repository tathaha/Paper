package io.papermc.generator.rewriter.data.sample.parser.imports;

import// discard?
    org /* hi */
        /* hi */ .
        bukkit /* hi */ // /*/
        /* hi */      .

        NamespacedKey// discard?
    /* hi */; // multi line import
;;;;; // will fail in Java 21+ but only in imports!

;;
;;;import// discard?
    static// discard?
    org// discard?
        /* hi */    . /* hi */

        /* hi */    bukkit /* hi */
        /* hi */     .
        /* a */
        // a

    Material

    .
    *// discard?
    ; // multi line star import
;;
public class MixedCommentImportType {

}
