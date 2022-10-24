package kittoku.osc.util

import kotlin.math.pow

fun <G> ArrayList<G>.sortByTOPSIS(weight: Array<Float>? = null, profits: Array<Boolean>? = null, parse: (data: G) -> Array<Double>): ArrayList<G> {
    val data = arrayListOf<Pair<G, Array<Double>>>()

    // collect data
    forEachIndexed { index, g ->
        val last = data.lastOrNull()?.second?.size
        val criteria = parse.invoke(g)
        if (last != null && last != criteria.size) {
            throw IllegalArgumentException("parsed item size for index $index should be like another items!")
        }
        data.add(g to criteria)
    }

    normalize(data)
    weightMultiply(weight, data)
    return calculateBestAndWorse(profits, data).toArrayList()
}

private fun <G> normalize(data: ArrayList<Pair<G, Array<Double>>>) {
    val distance = arrayListOf<Double>()
    for (i in 0 until (data.first().second.size)) {
        var sum = 0.0
        data.forEach {
            sum += it.second[i].toDouble().pow(2.0)
        }
        distance.add(sum.pow(0.5))
    }

    for (i in 0 until (data.first().second.size)) {
        data.forEachIndexed { index, pair ->
            data[index].second[i] = pair.second[i] / distance[i]
        }
    }
}

private fun <G> weightMultiply(weight: Array<Float>?, data: ArrayList<Pair<G, Array<Double>>>) {
    if (weight.isNullOrEmpty()) return
    if (weight.size != data.first().second.size) throw IllegalArgumentException("weights size should equals to criteria")

    for (i in 0 until (data.first().second.size)) {
        data.forEachIndexed { index, pair ->
            data[index].second[i] = pair.second[i] * weight[i]
        }
    }
}

private fun <G> calculateBestAndWorse(profits: Array<Boolean>?, data: ArrayList<Pair<G, Array<Double>>>): List<G> {
    // first => best  |  second => worse
    val rankHolder = arrayListOf<Pair<G, Double>>()
    val bestAndWorse = arrayListOf<Pair<Double, Double>>()
    for (i in 0 until (data.first().second.size)) {
        var best = data.first().second[i] * 1.0
        var worse = data.first().second[i] * 1.0
        data.forEachIndexed { index, pair ->
            if (profits?.getOrElse(i) { true }!!) {
                if (pair.second[i] > best)
                    best = pair.second[i]

                if (pair.second[i] < worse)
                    worse = pair.second[i]
            } else {
                if (pair.second[i] < best)
                    best = pair.second[i]

                if (pair.second[i] > worse)
                    worse = pair.second[i]
            }
        }
        bestAndWorse.add(best to worse)
    }

    data.forEachIndexed { index, pair ->
        var positive = .0
        var negative = .0
        pair.second.forEachIndexed { index, d ->
            positive += (d - (bestAndWorse[index].first)).pow(2.0)
            negative += (d - (bestAndWorse[index].second)).pow(2.0)
        }
        positive = positive.pow(0.5)
        negative = negative.pow(0.5)
        val performance = negative / (positive + negative)
        rankHolder.add(pair.first to performance)
    }

    rankHolder.sortByDescending { it.second }

    return rankHolder.map { it.first }
}

