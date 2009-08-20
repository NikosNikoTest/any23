package org.deri.any23.extractor.html;

import java.io.IOException;
import java.util.Arrays;

import org.deri.any23.extractor.ExtractionException;
import org.deri.any23.extractor.ExtractionResult;
import org.deri.any23.extractor.ExtractorDescription;
import org.deri.any23.extractor.ExtractorFactory;
import org.deri.any23.extractor.SimpleExtractorFactory;
import org.deri.any23.extractor.Extractor.TagSoupDOMExtractor;
import org.deri.any23.rdf.Any23ValueFactoryWrapper;
import org.deri.any23.rdf.PopularPrefixes;
import org.openrdf.model.BNode;
import org.openrdf.model.URI;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.w3c.dom.Document;

/**
 * Extractor for "ICBM coordinates" provided as META headers in the head
 * of an HTML page.
 * 
 * @author Gabriele Renzi
 * @author Richard Cyganiak (richard@cyganiak.de)
 */
public class ICBMExtractor implements TagSoupDOMExtractor {
	
	public void run(Document in, URI documentURI, ExtractionResult out) throws IOException,
	ExtractionException {

		// ICBM is the preferred method, if two values are available it is meaningless to read both
		String props = DomUtils.find(in, "//META[@name=\"ICBM\" or @name=\"geo.position\"]/@content");
		if ("".equals(props)) return;
		
		String[] coords = props.split("[;,]");
		float lat, lon;
		try {
			lat = Float.parseFloat(coords[0]);
			lon = Float.parseFloat(coords[1]);
		} catch (NumberFormatException nfe) {
			return;
		}

		ValueFactory factory = new Any23ValueFactoryWrapper(ValueFactoryImpl.getInstance());

		BNode point = factory.createBNode();
		out.writeTriple(documentURI, expand("dcterms:related"), point);
		out.writeTriple(point, expand("rdf:type"), expand("geo:Point"));
		out.writeTriple(point, expand("geo:lat"), factory.createLiteral(Float.toString(lat)));
		out.writeTriple(point, expand("geo:long"), factory.createLiteral(Float.toString(lon)));
	}
	
	private URI expand(String curie) {
		return factory.getPrefixes().expand(curie);
	}
	
	public ExtractorDescription getDescription() {
		return factory;
	}
	
	public final static ExtractorFactory<ICBMExtractor> factory = 
		SimpleExtractorFactory.create(
				"html-head-icbm",
				PopularPrefixes.createSubset("geo", "rdf"),
				Arrays.asList("text/html;q=0.01", "application/xhtml+xml;q=0.01"),
				null,
				ICBMExtractor.class);
}