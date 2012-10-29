package models.helper

import org.codehaus.jackson.annotate.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
case class BelgacomListing(
    tvmovies:BelgacomResult,
    tvgrid :BelgacomResult) {
}