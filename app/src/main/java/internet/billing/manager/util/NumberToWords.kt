package internet.billing.manager.util

object NumberToWords {
    private val units = arrayOf(
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
        "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen",
        "Seventeen", "Eighteen", "Nineteen"
    )

    private val tens = arrayOf(
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    )

    fun convertToWords(amount: Long): String {
        if (amount == 0L) return "Zero Taka Only"

        val sb = StringBuilder()
        var num = amount

        if (num >= 10000000) { // Crore
            sb.append(convertToWordsSection((num / 10000000).toInt())).append(" Crore ")
            num %= 10000000
        }

        if (num >= 100000) { // Lakh
            sb.append(convertToWordsSection((num / 100000).toInt())).append(" Lakh ")
            num %= 100000
        }

        if (num >= 1000) { // Thousand
            sb.append(convertToWordsSection((num / 1000).toInt())).append(" Thousand ")
            num %= 1000
        }

        if (num > 0) {
            sb.append(convertToWordsSection(num.toInt()))
        }

        return "${sb.toString().trim()} Taka Only"
    }

    private fun convertToWordsSection(n: Int): String {
        var num = n
        val sb = StringBuilder()

        if (num >= 100) {
            sb.append(units[num / 100]).append(" Hundred ")
            num %= 100
        }

        if (num > 0) {
            if (num < 20) {
                sb.append(units[num])
            } else {
                sb.append(tens[num / 10])
                if (num % 10 > 0) {
                    sb.append(" ").append(units[num % 10])
                }
            }
        }

        return sb.toString().trim()
    }
}
