package guru.springframework.spring6restmvc.service;

import guru.springframework.spring6restmvc.entities.Beer;
import guru.springframework.spring6restmvc.mappers.IBeerMapper;
import guru.springframework.spring6restmvc.model.BeerDTO;
import guru.springframework.spring6restmvc.model.BeerStyle;
import guru.springframework.spring6restmvc.repositories.IBeerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Primary
@RequiredArgsConstructor
public class BeerServiceJPA implements IBeerService {
    private final IBeerRepository beerRepository;
    private final IBeerMapper beerMapper;

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 25;

    @Override
    public Page<BeerDTO> listBeers(String beerName, BeerStyle beerStyle, Boolean showInventory, Integer pageNumber, Integer pageSize) {

        PageRequest pageRequest = _buildPageRequest(pageNumber, pageSize);

        Page<Beer> beerPage;

        if( StringUtils.hasText(beerName) ) {
            if ( beerStyle == null) {
                beerPage = _listBeersByName(beerName, pageRequest);
            } else {
                beerPage = _listBeersByNameAndStyle(beerName, beerStyle, pageRequest);
            }
        } else {
            if ( beerStyle == null) {
                beerPage = beerRepository.findAll(pageRequest);
            }
            else {
                beerPage = _listBeersByStyle(beerStyle, pageRequest);
            }
        }

        if(showInventory != null && !showInventory) {
            beerPage.forEach(beer -> beer.setQuantityOnHand(null));
        }

        // returning empty list is fine
//        return beerPage
//                .stream()
//                .map(beerMapper::beerToBeerDto)
//                .collect(Collectors.toList());
        return beerPage.map(beerMapper::beerToBeerDto);
    }

    private Page<Beer> _listBeersByName(String beerName, PageRequest pageRequest) {
        // LIKE function needs '%' - wildcard
        return beerRepository.findAllByBeerNameIsLikeIgnoreCase("%" + beerName + "%", pageRequest);
    }

    private Page<Beer> _listBeersByStyle(BeerStyle beerStyle, PageRequest pageRequest) {
        return beerRepository.findAllByBeerStyle(beerStyle, pageRequest);
    }

    private Page<Beer> _listBeersByNameAndStyle(String beerName, BeerStyle beerStyle, PageRequest pageRequest) {
        return beerRepository.findAllByBeerNameIsLikeIgnoreCaseAndBeerStyle("%" + beerName + "%", beerStyle, pageRequest);
    }

    @Override
    public Optional<BeerDTO> getBeerById(UUID id) {
        // validation is required

        return Optional.ofNullable(beerMapper.beerToBeerDto(beerRepository.findById(id).orElse(null)));
    }

    @Override
    public BeerDTO saveNewBeer(BeerDTO beer) {
        return beerMapper.beerToBeerDto(beerRepository.save(beerMapper.beerDtoToBeer(beer)));
    }

    @Override
    public Optional<BeerDTO> updateBeerById(UUID beerId, BeerDTO beer) {
        // being able to reference stuff from Lambda functions outside
        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();

        beerRepository.findById(beerId).ifPresentOrElse(foundBeer -> {
            foundBeer.setBeerName(beer.getBeerName());
            foundBeer.setBeerStyle(beer.getBeerStyle());
            foundBeer.setUpc(beer.getUpc());
            //foundBeer.setVersion(beer.getVersion());
            foundBeer.setPrice(beer.getPrice());
            foundBeer.setQuantityOnHand(beer.getQuantityOnHand());
            atomicReference.set(Optional.of(beerMapper
                    .beerToBeerDto((beerRepository.save(foundBeer)))));
        }, () -> {
            atomicReference.set(Optional.empty());
        });

        // DETACHED MODE alternative, that would catch failures of OptimisticLocks faster ..
        // just take object and try to update
        // return Optional.of(beerMapper.beerToBeerDto( beerRepository.save(beerMapper.beerDtoToBeer(beer)) ))

        return atomicReference.get();
    }

    @Override
    public Boolean deleteById(UUID id) {
        // simple check, no pojo parsing
        if(beerRepository.existsById(id)) {
            beerRepository.deleteById(id);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Optional<BeerDTO> patchById(UUID beerId, BeerDTO beer) {
        AtomicReference<Optional<BeerDTO>> atomicReference = new AtomicReference<>();

        beerRepository.findById(beerId).ifPresentOrElse(foundBeer -> {
            if(StringUtils.hasText(beer.getBeerName())) {
                foundBeer.setBeerName(beer.getBeerName());
            }
            if(beer.getBeerStyle() != null ) {
                foundBeer.setBeerStyle(beer.getBeerStyle());
            }
            if(beer.getPrice() != null) {
                foundBeer.setPrice(beer.getPrice());
            }
            if (beer.getQuantityOnHand() != null) {
                foundBeer.setQuantityOnHand(beer.getQuantityOnHand());
            }
            if(StringUtils.hasText(beer.getUpc())) {
                foundBeer.setUpc(beer.getUpc());
            }
            atomicReference.set(Optional.of(beerMapper.beerToBeerDto(beerRepository.save(foundBeer))));
        }, () -> {
            atomicReference.set(Optional.empty());
        });

        return atomicReference.get();
    }

    private PageRequest _buildPageRequest(Integer pageNumber, Integer pageSize) {
        int queryPageNumber;
        int queryPageSize;

        if(pageNumber != null && pageNumber > 0 ) {
            queryPageNumber = pageNumber - 1;
        } else {
            queryPageNumber = DEFAULT_PAGE;
        }

        if(pageSize != null) {
            if(pageSize > 1000) {
                queryPageSize = 1000;
                // can add warning log
            } else {
                queryPageSize = pageSize;
            }
        } else {
            queryPageSize = DEFAULT_SIZE;
        }

        Sort sort = Sort.by(Sort.Order.asc("beerName"));

        return PageRequest.of(queryPageNumber, queryPageSize, sort);
    }
}
