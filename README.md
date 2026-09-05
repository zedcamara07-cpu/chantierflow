# ChantierFlow — version initiale 0.1

Application web responsive de gestion de chantier, avec configuration Android Capacitor.

## État de livraison

Le code fourni doit être installé, testé et compilé dans votre environnement.
Il ne constitue pas une application complète auditée et prête pour des données sensibles.

Fonctionnalités implémentées :
- authentification Supabase ;
- création d'entreprise ;
- lecture des entreprises auxquelles l'utilisateur appartient ;
- chantiers : création, modification, archivage, suppression si sans dépendances ;
- tâches : création, modification, statut, priorité, échéance ;
- rapports : création, modification, soumission, validation par responsable ;
- dépenses TTC en EUR : création, modification, paiement ;
- photos privées : compression côté client, téléversement, consultation ;
- tableau de bord et recherche ;
- APK Android de développement via GitHub Actions.

Non implémenté :
- invitations et administration d'équipe depuis l'interface ;
- affectation nominative des tâches ;
- pointage, planning avancé, réserves, matériaux ;
- espace client ;
- notifications, temps réel, export PDF et export CSV ;
- mode hors ligne et résolution de conflits ;
- journal d'audit et révisions de rapports ;
- publication Play Store et signature de production ;
- PWA installable avec service worker ;
- thème sombre.

## Prérequis

- Node.js 22 et npm ;
- un projet Supabase ;
- Python 3 uniquement pour le script générateur ;
- Android Studio et Java 21 uniquement pour compiler Android localement.

## Installation

1. Exécuter `supabase/001_initial.sql` UNE FOIS dans l'éditeur SQL d'un projet Supabase neuf.
2. Copier `.env.example` vers `.env`.
3. Renseigner :
   - `VITE_SUPABASE_URL`
   - `VITE_SUPABASE_ANON_KEY`
4. Exécuter :

```bash
npm install
npm test
npm run dev
```

Le terminal indique l'adresse de développement.

IMPORTANT :
- utiliser la clé publique anon, jamais service_role ;
- les clés VITE sont incluses dans l'application cliente ;
- la sécurité dépend des politiques RLS et Storage ;
- générer et versionner package-lock.json après la première installation validée.

## Supabase Auth

Configurer dans Authentication / URL Configuration :
- Site URL : URL de votre application web ;
- Redirect URLs : URL locale de développement et URL web de production.

L'inscription peut demander une confirmation d'e-mail selon les paramètres du projet.

Le lien de récupération du mot de passe utilise l'URL web configurée.
Cette version ne configure pas le retour automatique dans l'APK via deep link.

Pour un usage réel, configurer le fournisseur SMTP et vérifier les limites d'envoi,
la protection anti-abus et les paramètres d'expiration des sessions.

## Premier lancement

1. Créer un compte.
2. Confirmer l'e-mail si nécessaire.
3. Se connecter.
4. Créer l'entreprise.
5. Créer un chantier.
6. Ajouter tâches, rapports, dépenses et photos.

Il n'existe aucun compte de démonstration public ni mot de passe codé en dur.

## Permissions

| Action | Propriétaire | Responsable | Collaborateur |
| --- | --- | --- | --- |
| Lire les chantiers, tâches, rapports, photos | oui | oui | oui |
| Créer/modifier un chantier | oui | oui | non |
| Créer/modifier une tâche | oui | oui | oui |
| Créer/modifier un rapport non validé | oui | oui | oui |
| Valider un rapport soumis | oui | oui | non |
| Lire/créer/modifier les dépenses | oui | oui | non |
| Ajouter une photo | oui | oui | oui |
| Supprimer un élément métier non verrouillé | oui | oui | non |

Les collaborateurs ont accès aux données opérationnelles de toute leur entreprise :
les affectations et restrictions par chantier ne sont pas encore implémentées.

Aucun utilisateur ne peut modifier ses droits depuis l'API cliente.
L'administration des membres reste manuelle dans cette version.

Pour ajouter un membre en phase de test :
- créer d'abord son compte Auth ;
- récupérer son UUID dans Supabase ;
- ajouter une ligne organization_members depuis l'interface administrateur Supabase ;
- choisir explicitement owner, manager ou member.

Ne pas donner accès à la console Supabase aux utilisateurs ordinaires.

## Rapports

Un rapport commence en brouillon.
Il peut être soumis, puis validé par un propriétaire ou responsable.
Un rapport validé ne peut plus être modifié ou supprimé.
Le mécanisme de révision reste à développer.

## Photos

- stockage privé ;
- lien signé valable une heure ;
- actualiser l'écran si un lien expire ;
- réencodage JPEG côté client et largeur/hauteur maximale de 1920 px ;
- originaux non conservés ;
- JPG, PNG, WebP ; HEIC non géré explicitement.

Le réencodage produit une nouvelle image sans recopier volontairement les métadonnées.
Vérifier ce comportement sur les appareils ciblés.

Les restrictions de taille et MIME sont configurées côté Storage.
Il n'y a pas encore d'analyse antivirus ou de validation serveur approfondie du contenu.
L'import direct depuis l'API peut contourner la compression côté client.

Stockage et métadonnées ne sont pas transactionnels :
une coupure pendant l'envoi/suppression peut laisser un fichier orphelin ou
une ligne pointant vers un fichier supprimé. Prévoir un outil de réconciliation
avant production.

## Limites de données et concurrence

- maximum 500 lignes chargées par module ;
- les indicateurs portent sur les lignes chargées, pas sur un agrégat serveur exhaustif ;
- pas de pagination au-delà de cette limite ;
- rechargement manuel, pas de synchronisation temps réel ;
- les modifications ordinaires sont en last-write-wins ;
- aucune file d'attente hors ligne ;
- toute écriture nécessite Internet ;
- les dates de chantier sont des dates civiles ;
- les horodatages techniques sont stockés en timestamptz ;
- les dépenses sont TTC en EUR, sans moteur TVA ni comptabilité certifiée.

## APK via GitHub Actions

1. Déposer le contenu du dossier à la racine du dépôt GitHub.
2. Ouvrir Settings > Secrets and variables > Actions.
3. Créer les secrets :
   - VITE_SUPABASE_URL
   - VITE_SUPABASE_ANON_KEY
4. Ouvrir Actions > Android - APK de test > Run workflow.
5. Après succès, télécharger l'artefact ChantierFlow-Android-Debug.
6. Extraire app-debug.apk et l'installer sur un appareil Android de test.

Il peut être nécessaire d'autoriser l'installation depuis cette source sur Android.

C'est un APK debug, pas une version Play Store.
La clé debug d'un runner GitHub peut changer entre deux builds :
une désinstallation peut être nécessaire avant d'installer une nouvelle compilation.
Ne pas adopter ce mécanisme comme stratégie de distribution finale.

Les quotas et conditions de GitHub Actions et Supabase doivent être vérifiés
dans vos comptes. Aucun coût ou publication ne doit être engagé sans votre accord.

## Android local

```bash
npm install
npm run build
npx cap add android
npx cap sync android
npx cap open android
```

La commande `cap add android` n'est nécessaire qu'une fois dans un dossier local.
Le projet Android est généré et ignoré par Git dans cette version.

## Tests inclus

Tests unitaires :
- conversion des montants en centimes ;
- rejet des formats monétaires invalides ;
- calcul de progression des tâches.

À ajouter avant production :
- tests d'intégration RLS avec deux entreprises ;
- tests de l'API avec un rôle collaborateur ;
- tests d'accès au stockage privé ;
- tests de bout en bout ;
- tests Android réels ;
- tests des formulaires avec réseau instable ;
- audit accessibilité ;
- analyse des dépendances.

## Checklist de production

- [ ] Valider les règles métier avec l'entreprise.
- [ ] Exécuter les tests d'isolation interentreprises.
- [ ] Ajouter tests automatisés d'intégration et E2E.
- [ ] Implémenter pagination et agrégats serveur.
- [ ] Ajouter journal d'audit et gestion des conflits.
- [ ] Durcir et tester la chaîne d'import de fichiers.
- [ ] Configurer sauvegardes de base ET de fichiers et tester une restauration.
- [ ] Définir conservation, export et suppression des données.
- [ ] Configurer SMTP, anti-abus et supervision.
- [ ] Examiner les dépendances et versionner le lockfile.
- [ ] Prévoir polices auto-hébergées si nécessaire : cette version utilise Google Fonts.
- [ ] Vérifier la confidentialité, les mentions légales et les obligations applicables.
- [ ] Créer une signature Android de production sécurisée.
- [ ] Tester sur les appareils réellement utilisés.

Aucune certification RGPD, audit de sécurité ou garantie de compilation
n'est revendiquée par cette livraison.
