// Copyright (c) 2026 eccenca GmbH
// Licensed under the GNU Affero General Public License v3 (AGPL-3.0),
// as part of the Openllet distribution.
package com.eccenca.openllet;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.Locale;

import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;

import openllet.jena.ModelExtractor;
import openllet.jena.ModelExtractor.StatementType;
import openllet.jena.PelletReasonerFactory;

/**
 * Materialize inferred axioms from an ontology and write them out as Turtle.
 *
 * <p>The stock {@code openllet extract} CLI command computes the inferred model via
 * {@link ModelExtractor} but never serializes it: its {@code output(model)} call sits
 * behind a dead {@code if (_debug)} branch ({@code _debug} is a hardcoded
 * {@code false}). This entry point reuses the exact same {@link ModelExtractor} and
 * {@link StatementType} selector semantics as the upstream command and actually writes
 * the result, so the plugin can shell out to {@code java} and read inferred Turtle back.
 *
 * <p>Usage: {@code ExtractInferences <input-file> <output-file> [statement ...]}.
 * Statement names match the {@code openllet extract -s} vocabulary (SubClassOf,
 * ClassAssertion, EquivalentClasses, ...). With no statement given, DefaultStatements
 * is used. owl:imports are not followed; callers are expected to pass a single,
 * pre-merged input file.
 */
public final class ExtractInferences {

    private ExtractInferences() {
    }

    public static void main(final String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println(
                "Usage: ExtractInferences <input-file> <output-file> [statement ...]");
            System.exit(2);
            return;
        }
        final String input = args[0];
        final String output = args[1];

        final EnumSet<StatementType> selector = EnumSet.noneOf(StatementType.class);
        if (args.length == 2) {
            selector.addAll(StatementType.DEFAULT_STATEMENTS);
        } else {
            for (int i = 2; i < args.length; i++) {
                selector.addAll(mapStatement(args[i]));
            }
        }

        final Model base = ModelFactory.createDefaultModel();
        RDFDataMgr.read(base, input);

        final InfModel inf = ModelFactory.createInfModel(
            PelletReasonerFactory.theInstance().create(null), base);
        inf.prepare();

        final ModelExtractor extractor = new ModelExtractor(inf);
        extractor.setSelector(selector);

        final Model extracted = ModelFactory.createDefaultModel();
        extractor.extractModel(extracted);

        try (OutputStream out = Files.newOutputStream(Paths.get(output))) {
            extracted.write(out, "TURTLE");
        }
    }

    /**
     * Map an {@code openllet extract -s} statement name to its StatementType set.
     * Mirrors {@code OpenlletExtractInferences.mapStatementTypes} verbatim.
     */
    private static EnumSet<StatementType> mapStatement(final String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "defaultstatements":
                return EnumSet.copyOf(StatementType.DEFAULT_STATEMENTS);
            case "allstatements":
                return EnumSet.copyOf(StatementType.ALL_STATEMENTS);
            case "allstatementsincludingjena":
                return EnumSet.copyOf(StatementType.ALL_STATEMENTS_INCLUDING_JENA);
            case "allclass":
                return EnumSet.copyOf(StatementType.ALL_CLASS_STATEMENTS);
            case "allindividual":
                return EnumSet.copyOf(StatementType.ALL_INDIVIDUAL_STATEMENTS);
            case "allproperty":
                return EnumSet.copyOf(StatementType.ALL_PROPERTY_STATEMENTS);
            case "classassertion":
                return EnumSet.of(StatementType.ALL_INSTANCE);
            case "complementof":
                return EnumSet.of(StatementType.COMPLEMENT_CLASS);
            case "datapropertyassertion":
                return EnumSet.of(StatementType.DATA_PROPERTY_VALUE);
            case "differentindividuals":
                return EnumSet.of(StatementType.DIFFERENT_FROM);
            case "directclassassertion":
                return EnumSet.of(StatementType.DIRECT_INSTANCE);
            case "directsubclassof":
                return EnumSet.of(StatementType.DIRECT_SUBCLASS);
            case "directsubpropertyof":
                return EnumSet.of(StatementType.DIRECT_SUBPROPERTY);
            case "disjointclasses":
                return EnumSet.of(StatementType.DISJOINT_CLASS);
            case "disjointproperties":
                return EnumSet.of(StatementType.DISJOINT_PROPERTY);
            case "equivalentclasses":
                return EnumSet.of(StatementType.EQUIVALENT_CLASS);
            case "equivalentproperties":
                return EnumSet.of(StatementType.EQUIVALENT_PROPERTY);
            case "inverseproperties":
                return EnumSet.of(StatementType.INVERSE_PROPERTY);
            case "objectpropertyassertion":
                return EnumSet.of(StatementType.OBJECT_PROPERTY_VALUE);
            case "propertyassertion":
                return EnumSet.copyOf(StatementType.PROPERTY_VALUE);
            case "sameindividual":
                return EnumSet.of(StatementType.SAME_AS);
            case "subclassof":
                return EnumSet.of(StatementType.ALL_SUBCLASS);
            case "subpropertyof":
                return EnumSet.of(StatementType.ALL_SUBPROPERTY);
            default:
                throw new IllegalArgumentException("Unknown statement type: " + name);
        }
    }
}
