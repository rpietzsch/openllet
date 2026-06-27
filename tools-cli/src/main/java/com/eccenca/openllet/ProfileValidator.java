// Copyright (c) 2026 eccenca GmbH
// Licensed under the GNU Affero General Public License v3 (AGPL-3.0),
// as part of the Openllet distribution.
package com.eccenca.openllet;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.profiles.OWL2DLProfile;
import org.semanticweb.owlapi.profiles.OWL2ELProfile;
import org.semanticweb.owlapi.profiles.OWL2Profile;
import org.semanticweb.owlapi.profiles.OWL2QLProfile;
import org.semanticweb.owlapi.profiles.OWL2RLProfile;
import org.semanticweb.owlapi.profiles.OWLProfile;

/**
 * Report which OWL 2 profiles an ontology conforms to, as a comma-separated list.
 *
 * <p>The Openllet CLI has no profile-validation subcommand (unlike ROBOT's
 * {@code validate-profile}), so this entry point uses the bundled OWL API profile
 * checkers directly. It mirrors the profile set and ordering of
 * {@code cmem-plugin-reason} (Full, DL, EL, QL, RL).
 *
 * <p>Usage: {@code ProfileValidator <input-file>}. Prints e.g. {@code Full,DL,EL,QL,RL}
 * (possibly empty) to stdout.
 */
public final class ProfileValidator {

    private ProfileValidator() {
    }

    public static void main(final String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: ProfileValidator <input-file>");
            System.exit(2);
            return;
        }
        final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        final OWLOntology ontology =
            manager.loadOntologyFromOntologyDocument(new File(args[0]));

        final OWLProfile[] profiles = {
            new OWL2Profile(),
            new OWL2DLProfile(),
            new OWL2ELProfile(),
            new OWL2QLProfile(),
            new OWL2RLProfile(),
        };
        final String[] names = {"Full", "DL", "EL", "QL", "RL"};

        final List<String> valid = new ArrayList<>();
        for (int i = 0; i < profiles.length; i++) {
            if (profiles[i].checkOntology(ontology).isInProfile()) {
                valid.add(names[i]);
            } else if ("Full".equals(names[i])) {
                // not even OWL 2 Full -> nothing else can hold
                break;
            }
        }
        System.out.println(String.join(",", valid));
    }
}
