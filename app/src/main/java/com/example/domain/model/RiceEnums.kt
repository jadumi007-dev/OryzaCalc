package com.example.domain.model

enum class RiceSeason(
    val code: String,
    val titleBn: String,
    val englishName: String,
    val seasonWindowBn: String,
    val waterNoteBn: String
) {
    AUS(
        code = "AUS",
        titleBn = "আউশ",
        englishName = "Aus",
        seasonWindowBn = "চৈত্র – আষাঢ় (মার্চ – জুলাই)",
        waterNoteBn = "প্রাক-বর্ষা ও বৃষ্টিনির্ভর মৌসুম; খরা দেখা দিলে সম্পূরক সেচ দিন।"
    ),
    AMAN(
        code = "AMAN",
        titleBn = "আমন",
        englishName = "Aman",
        seasonWindowBn = "আষাঢ় – অগ্রহায়ণ (জুন – ডিসেম্বর)",
        waterNoteBn = "বর্ষা মৌসুম; অতিরিক্ত জলাবদ্ধতা নিষ্কাশন ও কাইচ থোড়ে সম্পূরক সেচ জরুরি।"
    ),
    BORO(
        code = "BORO",
        titleBn = "বোরো",
        englishName = "Boro",
        seasonWindowBn = "অগ্রহায়ণ – বৈশাখ (নভেম্বর – মে)",
        waterNoteBn = "সেচনির্ভর শুষ্ক মৌসুম; AWD (পর্যায়ক্রমে ভেজানো ও শুকানো) পদ্ধতিতে সেচ খরচ কমবে।"
    );

    companion object {
        fun fromCode(code: String): RiceSeason =
            entries.firstOrNull { it.code == code } ?: BORO
    }
}

enum class LandUnit(
    val code: String,
    val labelBn: String,
    val decimalsPerUnit: Double
) {
    DECIMAL("DECIMAL", "শতাংশ", 1.0),
    BIGHA("BIGHA", "বিঘা", 33.0),
    HECTARE("HECTARE", "হেক্টর", 247.0),
    ACRE("ACRE", "একর", 100.0);

    companion object {
        fun fromCode(code: String): LandUnit =
            entries.firstOrNull { it.code == code } ?: BIGHA
    }
}

enum class GrowthPhase(
    val titleBn: String,
    val englishTitle: String
) {
    VEGETATIVE("দৈহিক বৃদ্ধি পর্যায়", "Vegetative Phase"),
    REPRODUCTIVE("প্রজনন পর্যায়", "Reproductive Phase"),
    RIPENING("পাকা পর্যায়", "Ripening Phase")
}
