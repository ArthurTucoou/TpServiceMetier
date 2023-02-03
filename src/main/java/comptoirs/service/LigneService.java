package comptoirs.service;

import comptoirs.dao.CommandeRepository;
import comptoirs.dao.LigneRepository;
import comptoirs.dao.ProduitRepository;
import comptoirs.entity.Ligne;
import comptoirs.entity.Produit;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated // Les contraintes de validatipn des méthodes sont vérifiées
public class LigneService {
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    private final CommandeRepository commandeDao;
    private final LigneRepository ligneDao;
    private final ProduitRepository produitDao;

    // @Autowired
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    public LigneService(CommandeRepository commandeDao, LigneRepository ligneDao, ProduitRepository produitDao) {
        this.commandeDao = commandeDao;
        this.ligneDao = ligneDao;
        this.produitDao = produitDao;
    }

    /**
     * <pre>
     * Service métier : 
     *     Enregistre une nouvelle ligne de commande pour une commande connue par sa clé,
     *     Incrémente la quantité totale commandée (Produit.unitesCommandees) avec la quantite à commander
     * Règles métier :
     *     - le produit référencé doit exister
     *     - la commande doit exister
     *     - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     *     - la quantité doit être positive
     *     - On doit avoir une quantite en stock du produit suffisante
     * <pre>
     * 
     *  @param commandeNum la clé de la commande
     *  @param produitRef la clé du produit
     *  @param quantite la quantité commandée (positive)
     *  @return la ligne de commande créée
     */
    @Transactional
    Ligne ajouterLigne(Integer commandeNum, Integer produitRef, @Positive int quantite) {
        //La commande doit exister
        var commande = commandeDao.findById(commandeNum).orElseThrow();
        if (commande == null) {
            throw new IllegalArgumentException("La commande n'existe pas");
        }
        if (commande.getEnvoyeele() != null) {
            throw new IllegalStateException("La commande a déjà été envoyée");
        }

        Produit produitref = produitDao.findById(produitRef).orElseThrow();
        if (produitref == null) {
            throw new IllegalArgumentException("Le produit n'existe pas");
        }
        if (produitref.getUnitesEnStock() < quantite) {
            throw new IllegalStateException("Stock insuffisant pour le produit " + produitref.getNom());
        }
        Ligne ligne = new Ligne(commande, produitref, quantite);
        commande.getLignes().add(ligne);
        produitref.setUnitesCommandees(produitref.getUnitesCommandees() + quantite);
        produitref.setUnitesEnStock(produitref.getUnitesEnStock() - quantite);
        return ligne;
    }
}
