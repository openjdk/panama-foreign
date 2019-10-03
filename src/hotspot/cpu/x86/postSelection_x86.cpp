//
// Copyright (c) 2003, 2019, Oracle and/or its affiliates. All rights reserved.
// DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
//
// This code is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License version 2 only, as
// published by the Free Software Foundation.
//
// This code is distributed in the hope that it will be useful, but WITHOUT
// ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
// FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
// version 2 for more details (a copy is included in the LICENSE file that
// accompanied this code).
//
// You should have received a copy of the GNU General Public License version
// 2 along with this work; if not, write to the Free Software Foundation,
// Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
//
// Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
// or visit www.oracle.com if you need additional information or have any
// questions.
//
//

#include "precompiled.hpp"
#include "adfiles/ad_x86.hpp"
#include "oops/compressedOops.hpp"
#include "opto/addnode.hpp"
#include "opto/cfgnode.hpp"
#include "opto/convertnode.hpp"
#include "opto/intrinsicnode.hpp"
#include "opto/matcher.hpp"
#include "opto/narrowptrnode.hpp"
#include "opto/opcodes.hpp"
#include "utilities/pair.hpp"

//----------------------------------------------------------------------
// Generic machine operands elision.
//----------------------------------------------------------------------

typedef struct {
  GrowableArray<Node *> *genvopernodes;
  GrowableArray<Node *> *lazyresolution;
} CollectionSet;

static MachNode *GetRCCNode(unsigned inOperand) {
  switch (inOperand) {
  default:
    assert(0, "Unhandled input operand in GetRCCNode");
  case VECS:
    return new MoveVecS2LegNode();
  case VECD:
    return new MoveVecD2LegNode();
  case VECX:
    return new MoveVecX2LegNode();
  case VECY:
    return new MoveVecY2LegNode();
  case VECZ:
    return new MoveVecZ2LegNode();
  case LEGVECS:
    return new MoveLeg2VecSNode();
  case LEGVECD:
    return new MoveLeg2VecDNode();
  case LEGVECX:
    return new MoveLeg2VecXNode();
  case LEGVECY:
    return new MoveLeg2VecYNode();
  case LEGVECZ:
    return new MoveLeg2VecZNode();
  }
}

static void GetRCCDefUseOpcodes(unsigned RccRule, int &def, int &use) {
  switch (RccRule) {
  default:
    assert(0, "Unhandled input operand in GetRCCNode");
  case MoveVecS2Leg_rule: {
    use = VECS;
    def = LEGVECS;
  } break;
  case MoveVecD2Leg_rule: {
    use = VECD;
    def = LEGVECD;
  } break;
  case MoveVecX2Leg_rule: {
    use = VECX;
    def = LEGVECX;
  } break;
  case MoveVecY2Leg_rule: {
    use = VECY;
    def = LEGVECY;
  } break;
  case MoveVecZ2Leg_rule: {
    use = VECZ;
    def = LEGVECZ;
  } break;
  case MoveLeg2VecS_rule: {
    use = LEGVECS;
    def = VECS;
  } break;
  case MoveLeg2VecD_rule: {
    use = LEGVECD;
    def = VECD;
  } break;
  case MoveLeg2VecX_rule: {
    use = LEGVECX;
    def = VECX;
  } break;
  case MoveLeg2VecY_rule: {
    use = LEGVECY;
    def = VECY;
  } break;
  case MoveLeg2VecZ_rule: {
    use = LEGVECZ;
    def = VECZ;
  } break;
  }
}

static MachOper *GetRCCOperand(unsigned opcode) {
  switch (opcode) {
  default:
    assert(0, "Unhandled input operand in GetRCCOperand");
  case VECS:
    return new vecSOper();
  case VECD:
    return new vecDOper();
  case VECX:
    return new vecXOper();
  case VECY:
    return new vecYOper();
  case VECZ:
    return new vecZOper();
  case LEGVECS:
    return new legVecSOper();
  case LEGVECD:
    return new legVecDOper();
  case LEGVECX:
    return new legVecXOper();
  case LEGVECY:
    return new legVecYOper();
  case LEGVECZ:
    return new legVecZOper();
  }
}

static bool IsLegacyOperand(int OpCode) {
  switch (OpCode) {
  default:
    return false;
  case LEGVECS:
  case LEGVECD:
  case LEGVECX:
  case LEGVECY:
  case LEGVECZ:
    return true;
  }
}

bool IsRCCNode(MachNode *mnode) {
  if (mnode->rule() == MoveVecS2Leg_rule ||
      mnode->rule() == MoveVecD2Leg_rule ||
      mnode->rule() == MoveVecX2Leg_rule ||
      mnode->rule() == MoveVecY2Leg_rule ||
      mnode->rule() == MoveVecZ2Leg_rule ||
      mnode->rule() == MoveLeg2VecX_rule ||
      mnode->rule() == MoveLeg2VecY_rule ||
      mnode->rule() == MoveLeg2VecZ_rule ||
      mnode->rule() == MoveLeg2VecS_rule || mnode->rule() == MoveLeg2VecD_rule)
    return true;
  else
    return false;
}

void CollectGenRegMovPre(Node &node, void *collectionset) {
  CollectionSet *cs = static_cast<CollectionSet *>(collectionset);
  GrowableArray<Node *> *genvopernodes = cs->genvopernodes;
  GrowableArray<Node *> *lazyresolution = cs->lazyresolution;

  if (!node.is_Mach())
    return;

  MachNode *mnode = (MachNode *)&node;
  for (unsigned i = 0; i < mnode->num_opnds(); i++) {
    if (mnode->_opnds[i]->opcode() != VECG)
      continue;

    // Collect MachTempNodes for lazy resolution.
    // TODO: Replace numerical rule with enum.
    if (mnode->rule() == 9999998)
      lazyresolution->append(&node);
    else
      genvopernodes->append(&node);
    break;
  }
}

void CollectGenRegMovPost(Node &node, void *workarray) {}

static MachOper *GetMachOperand(bool isLegacy, const TypeVect *vt) {
  if (isLegacy) {
    switch (vt->length_in_bytes()) {
    case 4:
      return new legVecSOper();
    case 8:
      return new legVecDOper();
    case 16:
      return new legVecXOper();
    case 32:
      return new legVecYOper();
    case 64:
      return new legVecZOper();
    default:
      assert(0, "Cannot find mach operand for this vector length");
    }
  } else {
    switch (vt->length_in_bytes()) {
    case 4:
      return new vecSOper();
    case 8:
      return new vecDOper();
    case 16:
      return new vecXOper();
    case 32:
      return new vecYOper();
    case 64:
      return new vecZOper();
    default:
      assert(0, "Cannot find mach operand for this vector length");
    }
  }
  return NULL;
}

static const  RegMask * GetRegMaskForType(bool isLegacy, const TypeVect *vt) {
  if (isLegacy) {
    switch (vt->length_in_bytes()) {
    case 4: {
      legVecSOper obj;
      return static_cast<MachOper*>(&obj)->in_RegMask(0);
     }
    case 8: {
      legVecDOper obj;
      return static_cast<MachOper*>(&obj)->in_RegMask(0);
     }
    case 16: {
      legVecXOper obj;
      return static_cast<MachOper*>(&obj)->in_RegMask(0);
     }
    case 32: {
      legVecYOper obj;
      return static_cast<MachOper*>(&obj)->in_RegMask(0);
     }
    case 64: {
      legVecZOper obj;
      return static_cast<MachOper*>(&obj)->in_RegMask(0);
     }
    default:
      assert(0, "Cannot find register mask for this vector length");
    }
  } else {
    switch (vt->length_in_bytes()) {
    case 4: {
      vecSOper obj;
      return static_cast<MachOper*>(&obj)->in_RegMask(0);
     }
    case 8: {
      vecDOper obj;
      return static_cast<MachOper*>(&obj)->in_RegMask(0);
     }
    case 16: {
      vecXOper obj;
      return static_cast<MachOper*>(&obj)->in_RegMask(0);
     }
    case 32: {
      vecYOper obj;
      return static_cast<MachOper*>(&obj)->in_RegMask(0);
     }
    case 64: {
      vecZOper obj;
      return static_cast<MachOper*>(&obj)->in_RegMask(0);
     }
    default:
      assert(0, "Cannot find register mask for this vector length");
    }
  }
  return NULL;
}

static MachOper *
GetSpecificOperandFromDef(MachNode *node, MachOper *oper,
                          GrowableArray<Node *> *lazyresolution) {
  int InEdgeIdx = node->operand_index(oper);
  assert(InEdgeIdx != -1, "Cannot find valid operand index");

  Node *in = node->in(InEdgeIdx);
  if (lazyresolution->contains(in))
    return NULL;

  if (!in->is_Mach()) {
    // Case for Data flow convergence nodes (Phi etc).
#ifndef PRODUCT
    tty->print(
        "Non-machine node found, returing operand based on node type.\n");
#endif
    bool IsLegacyUse = IsLegacyOperand(oper->opcode());
    return GetMachOperand(IsLegacyUse, in->bottom_type()->is_vect());
  } else {
    MachOper *inoper = static_cast<MachNode *>(in)->_opnds[0];
    if (inoper->opcode() != VECG && inoper->opcode() != LEGVECG)
      return inoper;
  }
  return oper;
}

void UpdateGenericTempUseOperand(MachNode *user, MachNode *def) {
  for (unsigned i = 1; i < user->num_opnds(); i++) {
    MachOper *op = user->_opnds[i];
    int op_idx = user->operand_index(op);
    if (user->in(op_idx) == def) {
      user->_opnds[i] = def->_opnds[0]->clone();
      break;
    }
  }
}

static void ReplaceGenericRCCNode(MachNode *RCC) {
  MachNode *NewRCC = GetRCCNode(RCC->_opnds[1]->opcode());
  RCC->replace_by(NewRCC);

  Node *Ctrl = RCC->in(0);
  Node *Inp = RCC->in(1);

  RCC->set_req(0, NULL);
  RCC->set_req(1, NULL);

  // Control edge initialization.
  NewRCC->add_req(Ctrl);
  NewRCC->add_req(Inp);

  // Create operand for new RCC node.
  int def, use;
  GetRCCDefUseOpcodes(NewRCC->rule(), def, use);
  NewRCC->_opnds[0] = GetRCCOperand(use);
  NewRCC->_opnds[1] = GetRCCOperand(def);
}

bool IsGenericRCCNode(MachNode *node) {
  return node->rule() == MoveVecG2LegVecG_rule ||
         node->rule() == MoveLegVecG2VecG_rule;
}

static int RemoveGenericOperands(Compile *c, Node *root) {
  CollectionSet cs;
  GrowableArray<Node *> lazyresolution;
  GrowableArray<Node *> genvopernodes;
  cs.genvopernodes = &genvopernodes;
  cs.lazyresolution = &lazyresolution;

  // Collect machine nodes having generic register operands.
  root->walk(CollectGenRegMovPre, CollectGenRegMovPost, &cs);

  int vec_nodes = lazyresolution.length() + genvopernodes.length();

  // Perform Iterative forward data flow propagation of specific
  // vector-length/register class operands till there are no-more
  // generic operands.
  while (genvopernodes.length()) {
    for (int i = 0; i < genvopernodes.length(); i++) {
      int GenOpndsCounter = 0;
      int GenTempOpndsCounter = 0;
      MachNode *mnode = static_cast<MachNode *>(genvopernodes.at(i));
      bool GenRCCNode = IsGenericRCCNode(mnode);

      // 1) Convert generic def operand into specific one using the type of
      // machine node.
      if (!GenRCCNode) {
        if (mnode->_opnds[0]->opcode() == VECG) {
          mnode->_opnds[0] =
              GetMachOperand(false, mnode->bottom_type()->is_vect());
        } else if (mnode->_opnds[0]->opcode() == LEGVECG) {
          mnode->_opnds[0] =
              GetMachOperand(true, mnode->bottom_type()->is_vect());
        }
      }

      // 2) For generic use operands pull specific register class operands from
      // its def instruction's output operand(def operand).
      for (unsigned j = 1; j < mnode->num_opnds(); j++) {
        if (mnode->_opnds[j]->opcode() == VECG ||
            mnode->_opnds[j]->opcode() == LEGVECG) {
          MachOper *inoper = GetSpecificOperandFromDef(mnode, mnode->_opnds[j],
                                                       &lazyresolution);
          if (inoper == NULL)
            GenTempOpndsCounter++;
          else if (inoper != mnode->_opnds[j]) {
            mnode->_opnds[j] = inoper->clone();
          } else
            GenOpndsCounter++;
        }
      }

      // 3) Handle MoveVecG2LegVecG and MoveLegVecG2VecG.
      if (GenRCCNode && mnode->_opnds[1]->opcode() != VECG &&
          mnode->_opnds[1]->opcode() != LEGVECG) {
        ReplaceGenericRCCNode(mnode);
      }

      if (0 == GenOpndsCounter)
        genvopernodes.remove(mnode);
    }
  }

  // Perform lazy resolution for MachTempNodes i.e. replacing their
  // generic operand with def operand of its user instruction.
  for (int i = 0; i < lazyresolution.length(); i++) {
    MachNode *mnode = static_cast<MachNode *>(lazyresolution.at(i));
    assert(mnode->outcnt() == 1, "Zero/Multi-user MachTempNode");

    MachNode *user = static_cast<MachNode *>(mnode->raw_out(0));
    MachOper *def = user->_opnds[0];
    assert(def->opcode() != VECG && def->opcode() != LEGVECG, "");

    mnode->_opnds[0] = def->clone();
    UpdateGenericTempUseOperand(user, mnode);
  }

  return vec_nodes;
}

static bool IsLegacyUseRCCNode(MachNode *mnode) {
  if (mnode->rule() == MoveLeg2VecX_rule ||
      mnode->rule() == MoveLeg2VecY_rule ||
      mnode->rule() == MoveLeg2VecZ_rule ||
      mnode->rule() == MoveLeg2VecS_rule || mnode->rule() == MoveLeg2VecD_rule)
    return true;
  else
    return false;
}

#ifdef PRODUCT
static const char *GetRCCName(unsigned RccRule) {
  switch (RccRule) {
  default:
    assert(0, "Unhandled input operand in GetRCCNode");
  case MoveVecS2Leg_rule:
    return "MoveVecS2Leg";
  case MoveVecD2Leg_rule:
    return "MoveVecD2Leg";
  case MoveVecX2Leg_rule:
    return "MoveVecX2Leg";
  case MoveVecY2Leg_rule:
    return "MoveVecY2Leg";
  case MoveVecZ2Leg_rule:
    return "MoveVecZ2Leg";
  case MoveLeg2VecS_rule:
    return "MoveLeg2VecS";
  case MoveLeg2VecD_rule:
    return "MoveLeg2VecD";
  case MoveLeg2VecX_rule:
    return "MoveLeg2VecX";
  case MoveLeg2VecY_rule:
    return "MoveLeg2VecY";
  case MoveLeg2VecZ_rule:
    return "MoveLeg2VecZ";
  }
}
#endif

void CollectLegacyUseRCCInstrsPre(Node &node, void *ws) {
  GrowableArray<Pair<Node *, Pair<Node *, int> > > *worklist =
      static_cast<GrowableArray<Pair<Node *, Pair<Node *, int> > > *>(ws);

  if (!node.is_Mach())
    return;

  MachNode *mnode = (MachNode *)&node;
  if (!IsLegacyUseRCCNode(mnode))
    return;

  for (DUIterator i = node.outs(); node.has_out(i); i++) {
    Node *user = node.out(i);
    if (!user->is_Mach())
      continue;
    MachNode *muser = static_cast<MachNode *>(user);
    for (unsigned i = 1; i < mnode->num_opnds(); i++) {
      MachOper *oper = muser->_opnds[i];
      int InEdgeIdx = muser->operand_index(oper);
      assert(InEdgeIdx > 0, "Incorrect input edge index");
      if (muser->in(InEdgeIdx) == &node) {
        Pair<Node *, int> userOpIdx(user, i);
        Pair<Node *, Pair<Node *, int> > rccUser(&node, userOpIdx);
        worklist->append(rccUser);
      }
    }
  }
}

void CollectLegacyUseRCCInstrsPost(Node &node, void *workarray) {}

//--------------------------------------------------------------------------
// Optimize away MoveLeg*2Vec* machine nodes, to prevent
// the generation of extra move instruction in the JITed code.
//--------------------------------------------------------------------------

static void RemoveLegacyOperandRCCNodes(Compile *c, Node *root) {
  // Collection Stage: Collect legacy RCC nodes and its users.
  // use's reg-clss(RegMask) strictly overlaps the def's reg-class(RegMask).
  GrowableArray<Pair<Node *, Pair<Node *, int> > > worklist;
  root->walk(CollectLegacyUseRCCInstrsPre, CollectLegacyUseRCCInstrsPost,
             &worklist);

  // Translation Stage: Remove RCC nodes with legacy use operand (MoveLeg*Vec*)
  // and update the user's operand appropriatly.
  for (int i = 0; i < worklist.length(); i++) {
    Pair<Node *, Pair<Node *, int> > item = worklist.at(i);
    MachNode *RCC = static_cast<MachNode *>(item.first);
    MachNode *user = static_cast<MachNode *>(item.second.first);
    int userOperIdx = item.second.second;
    int userInEdgeIdx = user->operand_index(userOperIdx);

    // Update user's operand to appropriate legacy operand.
    user->_opnds[userOperIdx] = RCC->_opnds[1]->clone();

    // Remove the RCC node.
    Node *def = RCC->in(1);
    RCC->set_req(1, NULL);
    user->set_req(userInEdgeIdx, NULL);
    user->init_req(userInEdgeIdx, def);

#ifdef PRODUCT
    tty->print("Removed legacy operand RCC node: %s\n",
               GetRCCName(RCC->rule()));
#endif
  }
}

const RegMask * Matcher::get_concrete_reg_mask(MachNode * node) {
   MachOper * def = node->_opnds[0];
   if (def->opcode() == VECG)
     return GetRegMaskForType(0, node->bottom_type()->is_vect());
   else if (def->opcode() == LEGVECG)
     return GetRegMaskForType(1, node->bottom_type()->is_vect());
   else 
     return &node->out_RegMask(); 
}

void Matcher::do_post_selection_processing(Compile *c, Node *root) {
  if (root && require_postselect_cleanup()) {
    Compile::_vec_nodes += RemoveGenericOperands(c, root);
    RemoveLegacyOperandRCCNodes(c, root);
  }
}
