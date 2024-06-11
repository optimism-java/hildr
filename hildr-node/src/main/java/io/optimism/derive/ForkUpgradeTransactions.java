package io.optimism.derive;

import java.util.List;

/**
 * The ForkUpgradeTransactions class.
 * Contains all the content of Forks upgrade transactions.
 *
 * @author thinkAfCod
 * @since 0.3.0
 */
public class ForkUpgradeTransactions {

    private ForkUpgradeTransactions() {}

    /**
     * The upgrade txs for ecotone.
     */
    public static final List<String> ECOTONE_UPGRADE_TXS = List.of(
            "0x7ef9059fa0877a6077205782ea15a6dc8699fa5ebcec5e0f4389f09cb8eda09488231346f89442100000000000000000000000000000000000008080808305b8d880b9055e608060405234801561001057600080fd5b5061053e806100206000396000f3fe608060405234801561001057600080fd5b50600436106100f55760003560e01c80638381f58a11610097578063c598591811610066578063c598591814610229578063e591b28214610249578063e81b2c6d14610289578063f82061401461029257600080fd5b80638381f58a146101e35780638b239f73146101f75780639e8c496614610200578063b80777ea1461020957600080fd5b806354fd4d50116100d357806354fd4d50146101335780635cf249691461017c57806364ca23ef1461018557806368d5dca6146101b257600080fd5b8063015d8eb9146100fa57806309bd5a601461010f578063440a5e201461012b575b600080fd5b61010d61010836600461044c565b61029b565b005b61011860025481565b6040519081526020015b60405180910390f35b61010d6103da565b61016f6040518060400160405280600581526020017f312e322e3000000000000000000000000000000000000000000000000000000081525081565b60405161012291906104be565b61011860015481565b6003546101999067ffffffffffffffff1681565b60405167ffffffffffffffff9091168152602001610122565b6003546101ce9068010000000000000000900463ffffffff1681565b60405163ffffffff9091168152602001610122565b6000546101999067ffffffffffffffff1681565b61011860055481565b61011860065481565b6000546101999068010000000000000000900467ffffffffffffffff1681565b6003546101ce906c01000000000000000000000000900463ffffffff1681565b61026473deaddeaddeaddeaddeaddeaddeaddeaddead000181565b60405173ffffffffffffffffffffffffffffffffffffffff9091168152602001610122565b61011860045481565b61011860075481565b3373deaddeaddeaddeaddeaddeaddeaddeaddead000114610342576040517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152603b60248201527f4c31426c6f636b3a206f6e6c7920746865206465706f7369746f72206163636f60448201527f756e742063616e20736574204c3120626c6f636b2076616c7565730000000000606482015260840160405180910390fd5b6000805467ffffffffffffffff98891668010000000000000000027fffffffffffffffffffffffffffffffff00000000000000000000000000000000909116998916999099179890981790975560019490945560029290925560038054919094167fffffffffffffffffffffffffffffffffffffffffffffffff00000000000000009190911617909255600491909155600555600655565b3373deaddeaddeaddeaddeaddeaddeaddeaddead00011461040357633cc50b456000526004601cfd5b60043560801c60035560143560801c600055602435600155604435600755606435600255608435600455565b803567ffffffffffffffff8116811461044757600080fd5b919050565b600080600080600080600080610100898b03121561046957600080fd5b6104728961042f565b975061048060208a0161042f565b9650604089013595506060890135945061049c60808a0161042f565b979a969950949793969560a0850135955060c08501359460e001359350915050565b600060208083528351808285015260005b818110156104eb578581018301518582016040015282016104cf565b818111156104fd576000604083870101525b50601f017fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe01692909201604001939250505056fea164736f6c634300080f000a",
            "0x7ef91016a0a312b4510adf943510f05fcc8f15f86995a5066bd83ce11384688ae20e6ecf42944210000000000000000000000000000000000001808080830f424080b90fd5608060405234801561001057600080fd5b50610fb5806100206000396000f3fe608060405234801561001057600080fd5b50600436106100f55760003560e01c806354fd4d5011610097578063de26c4a111610066578063de26c4a1146101da578063f45e65d8146101ed578063f8206140146101f5578063fe173b97146101cc57600080fd5b806354fd4d501461016657806368d5dca6146101af5780636ef25c3a146101cc578063c5985918146101d257600080fd5b8063313ce567116100d3578063313ce5671461012757806349948e0e1461012e5780634ef6e22414610141578063519b4bd31461015e57600080fd5b80630c18c162146100fa57806322b90ab3146101155780632e0f26251461011f575b600080fd5b6101026101fd565b6040519081526020015b60405180910390f35b61011d61031e565b005b610102600681565b6006610102565b61010261013c366004610b73565b610541565b60005461014e9060ff1681565b604051901515815260200161010c565b610102610565565b6101a26040518060400160405280600581526020017f312e322e3000000000000000000000000000000000000000000000000000000081525081565b60405161010c9190610c42565b6101b76105c6565b60405163ffffffff909116815260200161010c565b48610102565b6101b761064b565b6101026101e8366004610b73565b6106ac565b610102610760565b610102610853565b6000805460ff1615610296576040517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152602860248201527f47617350726963654f7261636c653a206f76657268656164282920697320646560448201527f707265636174656400000000000000000000000000000000000000000000000060648201526084015b60405180910390fd5b73420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff16638b239f736040518163ffffffff1660e01b8152600401602060405180830381865afa1580156102f5573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906103199190610cb5565b905090565b73420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff1663e591b2826040518163ffffffff1660e01b8152600401602060405180830381865afa15801561037d573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906103a19190610cce565b73ffffffffffffffffffffffffffffffffffffffff163373ffffffffffffffffffffffffffffffffffffffff1614610481576040517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152604160248201527f47617350726963654f7261636c653a206f6e6c7920746865206465706f73697460448201527f6f72206163636f756e742063616e2073657420697345636f746f6e6520666c6160648201527f6700000000000000000000000000000000000000000000000000000000000000608482015260a40161028d565b60005460ff1615610514576040517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152602660248201527f47617350726963654f7261636c653a2045636f746f6e6520616c72656164792060448201527f6163746976650000000000000000000000000000000000000000000000000000606482015260840161028d565b600080547fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff00166001179055565b6000805460ff161561055c57610556826108b4565b92915050565b61055682610958565b600073420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff16635cf249696040518163ffffffff1660e01b8152600401602060405180830381865afa1580156102f5573d6000803e3d6000fd5b600073420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff166368d5dca66040518163ffffffff1660e01b8152600401602060405180830381865afa158015610627573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906103199190610d04565b600073420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff1663c59859186040518163ffffffff1660e01b8152600401602060405180830381865afa158015610627573d6000803e3d6000fd5b6000806106b883610ab4565b60005490915060ff16156106cc5792915050565b73420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff16638b239f736040518163ffffffff1660e01b8152600401602060405180830381865afa15801561072b573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061074f9190610cb5565b6107599082610d59565b9392505050565b6000805460ff16156107f4576040517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152602660248201527f47617350726963654f7261636c653a207363616c61722829206973206465707260448201527f6563617465640000000000000000000000000000000000000000000000000000606482015260840161028d565b73420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff16639e8c49666040518163ffffffff1660e01b8152600401602060405180830381865afa1580156102f5573d6000803e3d6000fd5b600073420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff1663f82061406040518163ffffffff1660e01b8152600401602060405180830381865afa1580156102f5573d6000803e3d6000fd5b6000806108c083610ab4565b905060006108cc610565565b6108d461064b565b6108df906010610d71565b63ffffffff166108ef9190610d9d565b905060006108fb610853565b6109036105c6565b63ffffffff166109139190610d9d565b905060006109218284610d59565b61092b9085610d9d565b90506109396006600a610efa565b610944906010610d9d565b61094e9082610f06565b9695505050505050565b60008061096483610ab4565b9050600073420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff16639e8c49666040518163ffffffff1660e01b8152600401602060405180830381865afa1580156109c7573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906109eb9190610cb5565b6109f3610565565b73420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff16638b239f736040518163ffffffff1660e01b8152600401602060405180830381865afa158015610a52573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190610a769190610cb5565b610a809085610d59565b610a8a9190610d9d565b610a949190610d9d565b9050610aa26006600a610efa565b610aac9082610f06565b949350505050565b80516000908190815b81811015610b3757848181518110610ad757610ad7610f41565b01602001517fff0000000000000000000000000000000000000000000000000000000000000016600003610b1757610b10600484610d59565b9250610b25565b610b22601084610d59565b92505b80610b2f81610f70565b915050610abd565b50610aac82610440610d59565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b600060208284031215610b8557600080fd5b813567ffffffffffffffff80821115610b9d57600080fd5b818401915084601f830112610bb157600080fd5b813581811115610bc357610bc3610b44565b604051601f82017fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0908116603f01168101908382118183101715610c0957610c09610b44565b81604052828152876020848701011115610c2257600080fd5b826020860160208301376000928101602001929092525095945050505050565b600060208083528351808285015260005b81811015610c6f57858101830151858201604001528201610c53565b81811115610c81576000604083870101525b50601f017fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe016929092016040019392505050565b600060208284031215610cc757600080fd5b5051919050565b600060208284031215610ce057600080fd5b815173ffffffffffffffffffffffffffffffffffffffff8116811461075957600080fd5b600060208284031215610d1657600080fd5b815163ffffffff8116811461075957600080fd5b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b60008219821115610d6c57610d6c610d2a565b500190565b600063ffffffff80831681851681830481118215151615610d9457610d94610d2a565b02949350505050565b6000817fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0483118215151615610dd557610dd5610d2a565b500290565b600181815b80851115610e3357817fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff04821115610e1957610e19610d2a565b80851615610e2657918102915b93841c9390800290610ddf565b509250929050565b600082610e4a57506001610556565b81610e5757506000610556565b8160018114610e6d5760028114610e7757610e93565b6001915050610556565b60ff841115610e8857610e88610d2a565b50506001821b610556565b5060208310610133831016604e8410600b8410161715610eb6575081810a610556565b610ec08383610dda565b807fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff04821115610ef257610ef2610d2a565b029392505050565b60006107598383610e3b565b600082610f3c577f4e487b7100000000000000000000000000000000000000000000000000000000600052601260045260246000fd5b500490565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052603260045260246000fd5b60007fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff8203610fa157610fa1610d2a565b506001019056fea164736f6c634300080f000a",
            "0x7ef876a018acb38c5ff1c238a7460ebc1b421fa49ec4874bdf1e0a530d234104e5e67dbc940000000000000000000000000000000000000000944200000000000000000000000000000000000015808082c35080a43659cfe600000000000000000000000007dbe8500fc591d1852b76fee44d5a05e13097ff",
            "0x7ef876a0ee4f9385eceef498af0be7ec5862229f426dec41c8d42397c7257a5117d9230a94000000000000000000000000000000000000000094420000000000000000000000000000000000000f808082c35080a43659cfe6000000000000000000000000b528d11cc114e026f138fe568744c6d45ce6da7a",
            "0x7ef857a00c1cb38e99dbc9cbfab3bb80863380b0905290b37eb3d6ab18dc01c1f3e75f9394deaddeaddeaddeaddeaddeaddeaddeaddead000194420000000000000000000000000000000000000f808083013880808422b90ab3",
            "0x7ef8aaa069b763c48478b9dc2f65ada09b3d92133ec592ea715ec65ad6e7f3dc519dc00c940b799c86a49deeb90402691f1041aa3af2d3c8758080808303d09080b86a60618060095f395ff33373fffffffffffffffffffffffffffffffffffffffe14604d57602036146024575f5ffd5b5f35801560495762001fff810690815414603c575f5ffd5b62001fff01545f5260205ff35b5f5ffd5b62001fff42064281555f359062001fff015500");

    /**
     * The upgrade txs for fjord.
     */
    public static final List<String> FJORD_UPGRADE_TXS = List.of(
            "0x7ef91857a086122c533fdcb89b16d8713174625e44578a89751d96c098ec19ab40a51a8ea39442100000000000000000000000000000000000028080808316201080b91816608060405234801561001057600080fd5b506117f6806100206000396000f3fe608060405234801561001057600080fd5b50600436106101365760003560e01c80636ef25c3a116100b2578063de26c4a111610081578063f45e65d811610066578063f45e65d81461025b578063f820614014610263578063fe173b971461020d57600080fd5b8063de26c4a114610235578063f1c7a58b1461024857600080fd5b80636ef25c3a1461020d5780638e98b10614610213578063960e3a231461021b578063c59859181461022d57600080fd5b806349948e0e11610109578063519b4bd3116100ee578063519b4bd31461019f57806354fd4d50146101a757806368d5dca6146101f057600080fd5b806349948e0e1461016f5780634ef6e2241461018257600080fd5b80630c18c1621461013b57806322b90ab3146101565780632e0f262514610160578063313ce56714610168575b600080fd5b61014361026b565b6040519081526020015b60405180910390f35b61015e61038c565b005b610143600681565b6006610143565b61014361017d3660046112a1565b610515565b60005461018f9060ff1681565b604051901515815260200161014d565b610143610552565b6101e36040518060400160405280600581526020017f312e332e3000000000000000000000000000000000000000000000000000000081525081565b60405161014d9190611370565b6101f86105b3565b60405163ffffffff909116815260200161014d565b48610143565b61015e610638565b60005461018f90610100900460ff1681565b6101f8610832565b6101436102433660046112a1565b610893565b6101436102563660046113e3565b61098d565b610143610a69565b610143610b5c565b6000805460ff1615610304576040517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152602860248201527f47617350726963654f7261636c653a206f76657268656164282920697320646560448201527f707265636174656400000000000000000000000000000000000000000000000060648201526084015b60405180910390fd5b73420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff16638b239f736040518163ffffffff1660e01b8152600401602060405180830381865afa158015610363573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061038791906113fc565b905090565b3373deaddeaddeaddeaddeaddeaddeaddeaddead000114610455576040517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152604160248201527f47617350726963654f7261636c653a206f6e6c7920746865206465706f73697460448201527f6f72206163636f756e742063616e2073657420697345636f746f6e6520666c6160648201527f6700000000000000000000000000000000000000000000000000000000000000608482015260a4016102fb565b60005460ff16156104e8576040517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152602660248201527f47617350726963654f7261636c653a2045636f746f6e6520616c72656164792060448201527f616374697665000000000000000000000000000000000000000000000000000060648201526084016102fb565b600080547fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff00166001179055565b60008054610100900460ff16156105355761052f82610bbd565b92915050565b60005460ff16156105495761052f82610bdc565b61052f82610c80565b600073420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff16635cf249696040518163ffffffff1660e01b8152600401602060405180830381865afa158015610363573d6000803e3d6000fd5b600073420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff166368d5dca66040518163ffffffff1660e01b8152600401602060405180830381865afa158015610614573d6000803e3d6000fd5b505050506040513d601f19601f820116820180604052508101906103879190611415565b3373deaddeaddeaddeaddeaddeaddeaddeaddead0001146106db576040517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152603f60248201527f47617350726963654f7261636c653a206f6e6c7920746865206465706f73697460448201527f6f72206163636f756e742063616e20736574206973466a6f726420666c61670060648201526084016102fb565b60005460ff1661076d576040517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152603960248201527f47617350726963654f7261636c653a20466a6f72642063616e206f6e6c79206260448201527f65206163746976617465642061667465722045636f746f6e650000000000000060648201526084016102fb565b600054610100900460ff1615610804576040517f08c379a0000000000000000000000000000000000000000000000000000000008152602060048201526024808201527f47617350726963654f7261636c653a20466a6f726420616c726561647920616360448201527f746976650000000000000000000000000000000000000000000000000000000060648201526084016102fb565b600080547fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff00ff16610100179055565b600073420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff1663c59859186040518163ffffffff1660e01b8152600401602060405180830381865afa158015610614573d6000803e3d6000fd5b60008054610100900460ff16156108da57620f42406108c56108b484610dd4565b516108c090604461146a565b6110f1565b6108d0906010611482565b61052f91906114bf565b60006108e583611150565b60005490915060ff16156108f95792915050565b73420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff16638b239f736040518163ffffffff1660e01b8152600401602060405180830381865afa158015610958573d6000803e3d6000fd5b505050506040513d601f19601f8201168201806040525081019061097c91906113fc565b610986908261146a565b9392505050565b60008054610100900460ff16610a25576040517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152603660248201527f47617350726963654f7261636c653a206765744c314665655570706572426f7560448201527f6e64206f6e6c7920737570706f72747320466a6f72640000000000000000000060648201526084016102fb565b6000610a3283604461146a565b90506000610a4160ff836114bf565b610a4b908361146a565b610a5690601061146a565b9050610a61816111e0565b949350505050565b6000805460ff1615610afd576040517f08c379a000000000000000000000000000000000000000000000000000000000815260206004820152602660248201527f47617350726963654f7261636c653a207363616c61722829206973206465707260448201527f656361746564000000000000000000000000000000000000000000000000000060648201526084016102fb565b73420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff16639e8c49666040518163ffffffff1660e01b8152600401602060405180830381865afa158015610363573d6000803e3d6000fd5b600073420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff1663f82061406040518163ffffffff1660e01b8152600401602060405180830381865afa158015610363573d6000803e3d6000fd5b600061052f610bcb83610dd4565b51610bd790604461146a565b6111e0565b600080610be883611150565b90506000610bf4610552565b610bfc610832565b610c079060106114fa565b63ffffffff16610c179190611482565b90506000610c23610b5c565b610c2b6105b3565b63ffffffff16610c3b9190611482565b90506000610c49828461146a565b610c539085611482565b9050610c616006600a611646565b610c6c906010611482565b610c7690826114bf565b9695505050505050565b600080610c8c83611150565b9050600073420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff16639e8c49666040518163ffffffff1660e01b8152600401602060405180830381865afa158015610cef573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190610d1391906113fc565b610d1b610552565b73420000000000000000000000000000000000001573ffffffffffffffffffffffffffffffffffffffff16638b239f736040518163ffffffff1660e01b8152600401602060405180830381865afa158015610d7a573d6000803e3d6000fd5b505050506040513d601f19601f82011682018060405250810190610d9e91906113fc565b610da8908561146a565b610db29190611482565b610dbc9190611482565b9050610dca6006600a611646565b610a6190826114bf565b6060610f63565b818153600101919050565b600082840393505b838110156109865782810151828201511860001a1590930292600101610dee565b825b60208210610e5b578251610e26601f83610ddb565b52602092909201917fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe090910190602101610e11565b8115610986578251610e706001840383610ddb565b520160010192915050565b60006001830392505b6101078210610ebc57610eae8360ff16610ea960fd610ea98760081c60e00189610ddb565b610ddb565b935061010682039150610e84565b60078210610ee957610ee28360ff16610ea960078503610ea98760081c60e00189610ddb565b9050610986565b610a618360ff16610ea98560081c8560051b0187610ddb565b610f5b828203610f3f610f2f84600081518060001a8160011a60081b178160021a60101b17915050919050565b639e3779b90260131c611fff1690565b8060021b6040510182815160e01c1860e01b8151188152505050565b600101919050565b6180003860405139618000604051016020830180600d8551820103826002015b81811015611096576000805b50508051604051600082901a600183901a60081b1760029290921a60101b91909117639e3779b9810260111c617ffc16909101805160e081811c878603811890911b90911890915284019081830390848410610feb5750611026565b600184019350611fff8211611020578251600081901a600182901a60081b1760029190911a60101b1781036110205750611026565b50610f8f565b838310611034575050611096565b600183039250858311156110525761104f8787888603610e0f565b96505b611066600985016003850160038501610de6565b9150611073878284610e7b565b96505061108b8461108686848601610f02565b610f02565b915050809350610f83565b50506110a88383848851850103610e0f565b925050506040519150618000820180820391508183526020830160005b838110156110dd5782810151828201526020016110c5565b506000920191825250602001604052919050565b60008061110183620cc394611482565b61112b907ffffffffffffffffffffffffffffffffffffffffffffffffffffffffffd763200611652565b905061113b6064620f42406116c6565b81121561052f576109866064620f42406116c6565b80516000908190815b818110156111d35784818151811061117357611173611782565b01602001517fff00000000000000000000000000000000000000000000000000000000000000166000036111b3576111ac60048461146a565b92506111c1565b6111be60108461146a565b92505b806111cb816117b1565b915050611159565b50610a618261044061146a565b6000806111ec836110f1565b905060006111f8610b5c565b6112006105b3565b63ffffffff166112109190611482565b611218610552565b611220610832565b61122b9060106114fa565b63ffffffff1661123b9190611482565b611245919061146a565b905061125360066002611482565b61125e90600a611646565b6112688284611482565b610a6191906114bf565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052604160045260246000fd5b6000602082840312156112b357600080fd5b813567ffffffffffffffff808211156112cb57600080fd5b818401915084601f8301126112df57600080fd5b8135818111156112f1576112f1611272565b604051601f82017fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe0908116603f0116810190838211818310171561133757611337611272565b8160405282815287602084870101111561135057600080fd5b826020860160208301376000928101602001929092525095945050505050565b600060208083528351808285015260005b8181101561139d57858101830151858201604001528201611381565b818111156113af576000604083870101525b50601f017fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffe016929092016040019392505050565b6000602082840312156113f557600080fd5b5035919050565b60006020828403121561140e57600080fd5b5051919050565b60006020828403121561142757600080fd5b815163ffffffff8116811461098657600080fd5b7f4e487b7100000000000000000000000000000000000000000000000000000000600052601160045260246000fd5b6000821982111561147d5761147d61143b565b500190565b6000817fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff04831182151516156114ba576114ba61143b565b500290565b6000826114f5577f4e487b7100000000000000000000000000000000000000000000000000000000600052601260045260246000fd5b500490565b600063ffffffff8083168185168183048111821515161561151d5761151d61143b565b02949350505050565b600181815b8085111561157f57817fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff048211156115655761156561143b565b8085161561157257918102915b93841c939080029061152b565b509250929050565b6000826115965750600161052f565b816115a35750600061052f565b81600181146115b957600281146115c3576115df565b600191505061052f565b60ff8411156115d4576115d461143b565b50506001821b61052f565b5060208310610133831016604e8410600b8410161715611602575081810a61052f565b61160c8383611526565b807fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0482111561163e5761163e61143b565b029392505050565b60006109868383611587565b6000808212827f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff0384138115161561168c5761168c61143b565b827f80000000000000000000000000000000000000000000000000000000000000000384128116156116c0576116c061143b565b50500190565b60007f7fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff6000841360008413858304851182821616156117075761170761143b565b7f800000000000000000000000000000000000000000000000000000000000000060008712868205881281841616156117425761174261143b565b6000871292508782058712848416161561175e5761175e61143b565b878505871281841616156117745761177461143b565b505050929093029392505050565b7f4e487b7100000000000000000000000000000000000000000000000000000000600052603260045260246000fd5b60007fffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffffff82036117e2576117e261143b565b506001019056fea164736f6c634300080f000a",
            "0x7ef876a01e6bb0c28bfab3dc9b36ffb0f721f00d6937f33577606325692db0965a7d58c694000000000000000000000000000000000000000094420000000000000000000000000000000000000f808082c35080a43659cfe6000000000000000000000000a919894851548179a0750865e7974da599c0fac7",
            "0x7ef857a0bac7bb0d5961cad209a345408b0280a0d4686b1b20665e1b0f9cdafd73b19b6b94deaddeaddeaddeaddeaddeaddeaddeaddead000194420000000000000000000000000000000000000f808083015f9080848e98b106");
}