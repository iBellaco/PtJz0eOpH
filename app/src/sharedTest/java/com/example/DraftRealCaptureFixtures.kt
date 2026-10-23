package com.example

/** Independently transcribed full titles, not predictions from the recognizer. */
internal object DraftRealCaptureFixtures {
    data class Capture(val file:String,val sha256:String,val firstPick:Boolean,
        val titles:Map<Int,String>,val remainingAllyLane:com.example.model.LaneRole?,
        val tenth:String?=null)
    val captures=listOf(
        Capture("draft-swain-eight.jpg","5479ce8aea0396625cf3404a08aac2b588283f81210d85acc1ba7296a3692ac8",true,
            mapOf(0 to "swain",1 to "kayn",2 to "volibear",3 to "jhin",5 to "draven",6 to "zyra",7 to "vayne",8 to "yunara"),
            com.example.model.LaneRole.SUPPORT),
        Capture("draft-kaisa-nine.jpg","99fa9459a1378450c634169e0fb0e58687f5eb673ecf7ebf0046321075aedb3c",false,
            mapOf(0 to "seraphine",1 to "syndra",2 to "kai_sa",3 to "jarvan_iv",5 to "mel",6 to "tryndamere",7 to "xayah",8 to "k_sante",9 to "lux"),
            com.example.model.LaneRole.TOP),
        Capture("draft-mel-nine.jpg","38b6b08da840289a188ccc9401297893486bcaa88752c1e03256578330755103",true,
            mapOf(0 to "senna",1 to "ornn",2 to "mel",3 to "volibear",4 to "vi",5 to "alistar",6 to "caitlyn",7 to "nocturne",8 to "vex"),
            null,"teemo")
    )
}
