script {
                    // Encapsulate nexusIQScan in a closure
                    wrap([$class: 'BuildUser']) {
                        nexusIQScan()
